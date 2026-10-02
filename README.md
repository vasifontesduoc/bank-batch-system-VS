# Banco XYZ — Microservicios con Spring Cloud

Arquitectura de microservicios: 3 BFF independientes (uno por canal) + microservicio de datos, con Config Server, Service Discovery (Eureka) y tolerancia a fallos (Circuit Breaker).

## Estructura

eureka-server/ → Service Discovery. Puerto 8761.
config-server/ → Configuración centralizada. Puerto 8888.
ms-cuentas/ → Microservicio de datos. Único con acceso a MySQL. Puerto 8090.
bff-web/ → BFF Web. Puerto 8443 (HTTPS).
bff-mobile/ → BFF Móvil. Puerto 8444 (HTTPS).
bff-cajero/ → BFF Cajero. Puerto 8445 (HTTPS).
ms-fraude/ → Microservicio consumidor de eventos (deteccion de retiros sospechosos). Puerto 8092.
ms-notificaciones/ → Microservicio consumidor de eventos (simula notificacion al cliente). Puerto 8093.

## Arquitectura

Cliente Web → bff-web (8443) ─┐
Cliente Móvil → bff-mobile (8444) ─┼─► ms-cuentas (8090) → MySQL
Cajero → bff-cajero (8445) ─┘

Los 3 BFF se registran en eureka-server y consumen su configuración de config-server.

Cada BFF: tiene su propia API Key, su propio Circuit Breaker hacia `ms-cuentas`, y se registra en Eureka de forma independiente — son 3 microservicios completos, no un monolito dividido en rutas.

## Seguridad

| Microservicio | Puerto | Header |
|---|---|---|
| bff-web | 8443 | `X-API-KEY: web-2024-xyz-key` |
| bff-mobile | 8444 | `X-API-KEY: mobile-2024-xyz-key` |
| bff-cajero | 8445 | `X-API-KEY: cajero-2024-xyz-key` |

## Cómo ejecutar (en este orden)

```bash
docker compose up -d                       # Kafka (localhost:9092)
cd eureka-server && mvn spring-boot:run    # 8761
cd config-server && mvn spring-boot:run    # 8888
cd ms-cuentas && mvn spring-boot:run       # 8090
cd ms-fraude && mvn spring-boot:run        # 8092
cd ms-notificaciones && mvn spring-boot:run # 8093
cd bff-web && mvn spring-boot:run          # 8443
cd bff-mobile && mvn spring-boot:run       # 8444
cd bff-cajero && mvn spring-boot:run       # 8445
```

## Evidencia

Capturas de los 3 microservicios registrados en Eureka, funcionando de forma independiente, y del Circuit Breaker activándose en los 3 al mismo tiempo (con `ms-cuentas` apagado) están en el documento de entrega adjunto.

## Semana 7 — Arquitectura de eventos (Kafka)

Se agregó una arquitectura orientada a eventos usando **Apache Kafka**, siguiendo el patrón **Saga por coreografía**: `ms-cuentas` publica un evento cada vez que se confirma un retiro, y dos microservicios independientes lo consumen sin conocerse entre sí.

**Evento:** `RetiroRealizado` — publicado en el tópico `retiros-topic`.

**Productor:**
- `ms-cuentas`: al confirmar un retiro (`POST /internal/cuentas/{id}/retiro`), publica el evento con `cuentaId`, `monto`, `saldoResultante`, `descripcion` y `fecha`.

**Consumidores (mismo tópico, distinto consumer group cada uno):**
- `ms-fraude` (group: `fraude-group`): aplica la regla de anomalía (monto > 3000) y genera una alerta si el retiro es sospechoso.
- `ms-notificaciones` (group: `notificaciones-group`): simula el envío de una notificación al cliente confirmando el retiro.

Al tener consumer groups distintos, ambos microservicios reciben y procesan el mismo evento en paralelo, de forma totalmente desacoplada — ninguno depende del otro ni de `ms-cuentas` para funcionar.

**Kafka local:** se levanta con Docker (`docker-compose.yml` en la raíz, imagen `apache/kafka` en modo KRaft, sin necesidad de Zookeeper).

Diagrama de la arquitectura y evidencia de ejecución (los 2 consumidores reaccionando al mismo evento) en el documento de entrega adjunto.
