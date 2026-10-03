# Banco XYZ — Microservicios con Spring Cloud

Arquitectura de microservicios: 3 BFF independientes (uno por canal) + microservicios de dominio, con Config Server, Service Discovery (Eureka), seguridad OAuth2.0, tolerancia a fallos (Resilience4j), mensajería asíncrona (Kafka) y despliegue completo con Docker.

## Estructura

eureka-server/ → Service Discovery. Puerto 8761.
config-server/ → Configuración centralizada. Puerto 8888.
auth-server/ → Servidor de autorización OAuth2.0 (Spring Authorization Server). Puerto 9000.
ms-cuentas/ → Microservicio de datos. Único con acceso a MySQL. Puerto 8090.
ms-fraude/ → Microservicio consumidor de eventos (detección de retiros sospechosos). Puerto 8092.
ms-notificaciones/ → Microservicio consumidor de eventos (simula notificación al cliente). Puerto 8093.
bff-web/ → BFF Web. Puerto 8443 (HTTPS).
bff-mobile/ → BFF Móvil. Puerto 8444 (HTTPS).
bff-cajero/ → BFF Cajero. Puerto 8445 (HTTPS).

## Arquitectura

Cliente Web → bff-web (8443) ─┐
Cliente Móvil → bff-mobile (8444) ─┼─► ms-cuentas (8090) → MySQL
Cajero → bff-cajero (8445) ─┘ │
├─► publica evento (Kafka) ─┬─► ms-fraude (8092)
└─► ms-notificaciones (8093)

Los 3 BFF y los 3 microservicios de dominio se registran en `eureka-server` y consumen su configuración de `config-server`. Cada BFF tiene su propio Circuit Breaker hacia `ms-cuentas` y se registra en Eureka de forma independiente — son microservicios completos, no un monolito dividido en rutas.

## Seguridad: API Key + OAuth2.0

Cada BFF acepta **dos** formas de autenticación para sus rutas `/api/**` (cualquiera de las dos autentica la petición):

| Microservicio | Puerto | API Key (header `X-API-KEY`) |
|---|---|---|
| bff-web | 8443 | `web-2024-xyz-key` |
| bff-mobile | 8444 | `mobile-2024-xyz-key` |
| bff-cajero | 8445 | `cajero-2024-xyz-key` |

**OAuth2.0 (nuevo en S8):** `auth-server` es un Spring Authorization Server que emite JWT firmados (RSA) mediante el flujo `client_credentials`. Los 3 BFF actúan como Resource Server, validando ese JWT y mapeando el scope `api.access` al mismo rol `ROLE_API_CLIENT` que usa el API Key — ambos caminos de autenticación terminan satisfaciendo la misma regla de autorización.

```bash
# 1. Pedir un token
curl -X POST http://localhost:9000/oauth2/token \
  -u bank-client:bank-secret \
  -d "grant_type=client_credentials&scope=api.access"

# 2. Usarlo contra cualquier BFF
curl -k https://localhost:8443/api/web/cuentas/1/estado-anual \
  -H "Authorization: Bearer <access_token>"
```

Sin ninguno de los dos (ni API Key ni Bearer token), la ruta responde `401`.

## Tolerancia a fallos (Resilience4j)

Circuit Breaker + Retry, con método de fallback explícito, en **todos** los microservicios (no solo en los BFF):

- **bff-web / bff-mobile / bff-cajero** → Circuit Breaker hacia `ms-cuentas`.
- **ms-cuentas** → Retry sobre la publicación del evento a Kafka (`publicarEvento`), para no perder el evento ante una falla transitoria del broker.
- **ms-fraude** → Circuit Breaker + Retry (`procesarEvento`) alrededor de un servicio que simula un proveedor antifraude externo (`AntifraudeExternoService`).
- **ms-notificaciones** → Circuit Breaker + Retry (`enviarNotificacion`) alrededor de un servicio que simula un proveedor externo de SMS/email (`ProveedorNotificacionService`).

Cada uno puede forzarse a fallar vía una propiedad en el Config Server (`app.simular.falla.*=true`) para demostrar el fallback en vivo.

## Mensajería asíncrona (Kafka) — Saga por coreografía

`ms-cuentas` publica el evento `RetiroRealizado` (tópico `retiros-topic`) **después de confirmado el commit de la base de datos** (`TransactionSynchronizationManager.afterCommit`), nunca antes — así nunca se publica un evento de un retiro que terminó revertido. El productor es idempotente (`enable.idempotence=true`, `acks=all`).

Dos consumidores, cada uno en su propio consumer group, reciben el mismo evento de forma independiente:

- **ms-fraude** (`fraude-group`): aplica la regla de anomalía (monto > 3000).
- **ms-notificaciones** (`notificaciones-group`): simula el envío de una notificación al cliente.

**Idempotencia:** cada evento lleva un `eventId` (UUID); ambos consumidores descartan un evento ya procesado (dedup en memoria), para que un reintento o una redelivery de Kafka no duplique el efecto.

**Retries + DLQ:** si el procesamiento de un evento falla, Kafka reintenta 2 veces (1s de espera) y, si sigue fallando, lo manda a `retiros-topic-dlt` (Dead Letter Topic) en vez de perderlo o bloquear el consumidor.

## Docker

Cada uno de los 9 módulos tiene su propio `Dockerfile` multi-stage (build con Maven + runtime solo con JRE, imagen final liviana):

```bash
docker build -t ms-cuentas:s8 ./ms-cuentas
```

## docker-compose: stack completo

`docker-compose.yml` en la raíz levanta **todo el sistema** para un despliegue tipo Cloud: MySQL, Kafka, los 2 servidores de infraestructura (Eureka, Config Server), el servidor de autorización (`auth-server`), los 3 microservicios de dominio y los 3 BFF — todos conectados entre sí por nombre de servicio Docker (no `localhost`), en una red `bank-network`.

```bash
docker compose up --build
```

Al finalizar, verificar en `http://localhost:8761` que los 6 servicios de aplicación (`BFF-WEB`, `BFF-MOBILE`, `BFF-CAJERO`, `MS-CUENTAS`, `MS-FRAUDE`, `MS-NOTIFICACIONES`) aparecen `UP`.

| Servicio | Puerto host |
|---|---|
| eureka-server | 8761 |
| config-server | 8888 |
| auth-server | 9000 |
| mysql | 3307 → 3306 (contenedor) |
| kafka | 9092 |
| ms-cuentas | interno (8090, sin publicar al host) |
| ms-fraude | interno (8092) |
| ms-notificaciones | interno (8093) |
| bff-web | 8443 (HTTPS) |
| bff-mobile | 8444 (HTTPS) |
| bff-cajero | 8445 (HTTPS) |

## Cómo ejecutar

**Opción A — todo en Docker (recomendado para evaluar el sistema completo):**

```bash
docker compose up --build
```

**Opción B — manual, servicio por servicio (útil para desarrollo):**

```bash
docker compose up -d mysql kafka          # solo infraestructura de datos
cd eureka-server && mvn spring-boot:run   # 8761
cd config-server && mvn spring-boot:run   # 8888
cd auth-server && mvn spring-boot:run     # 9000
cd ms-cuentas && mvn spring-boot:run      # 8090
cd ms-fraude && mvn spring-boot:run       # 8092
cd ms-notificaciones && mvn spring-boot:run # 8093
cd bff-web && mvn spring-boot:run         # 8443
cd bff-mobile && mvn spring-boot:run      # 8444
cd bff-cajero && mvn spring-boot:run      # 8445
```

## Evidencia

Capturas de: los 6 servicios de aplicación registrados en Eureka; el Circuit Breaker/Retry activándose en los 3 microservicios de dominio (con fallback); el flujo OAuth2.0 completo (emisión de JWT y acceso autenticado solo con Bearer token); el DLQ recibiendo un evento tras agotar los reintentos; y el stack completo funcionando dentro de Docker — están en el documento de entrega adjunto (`Exp3_S8_Valeria_Sifontes`).
