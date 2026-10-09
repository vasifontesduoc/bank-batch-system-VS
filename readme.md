# EFT S9 — Desarrollo Backend Avanzado: Spring Cloud y Batch

**Curso:** Desarrollo Backend III (PBY2203) — Duoc UC
**Profesor:** Marcelo Zepeda
**Alumna:** Valeria Sifontes
**Modalidad:** Individual

## Repositorio de código

Todo el código fuente del proyecto está en GitHub:

👉 **https://github.com/vasifontesduoc/bank-batch-system-VS**

Rama: `main` — commit de esta entrega: `d333eea` ("Semana 9")

## Resumen del proyecto

Sistema bancario (Banco XYZ) construido como un ecosistema de microservicios con Spring Cloud, que evoluciona un sistema legacy de procesos batch hacia una arquitectura moderna, resiliente y orientada a eventos.

### Componentes

| Componente | Descripción | Puerto |
|---|---|---|
| `eureka-server` | Service discovery | 8761 |
| `config-server` | Configuración centralizada | 8888 |
| `auth-server` | Servidor OAuth2.0 (Spring Authorization Server) | 9000 |
| `ms-cuentas` | Gestión de Cuentas (datos, estados de cuenta, retiros) | 8090 |
| `ms-pagos` | Procesamiento de Pagos (transferencias/pagos, nuevo en S9) | 8094 |
| `ms-clientes` | Gestión de Clientes (datos personales, nuevo en S9) | 8095 |
| `ms-fraude` | Validación antifraude (consumidor Kafka) | 8092 |
| `ms-notificaciones` | Notificaciones al cliente (consumidor Kafka) | 8093 |
| `bff-web` | Backend-for-Frontend canal Web | 8443 (HTTPS) |
| `bff-mobile` | Backend-for-Frontend canal Móvil | 8444 (HTTPS) |
| `bff-cajero` | Backend-for-Frontend canal Cajero | 8445 (HTTPS) |
| `batch-jobs` | Jobs de Spring Batch (migración de 3 procesos legacy) | — |

### Características principales

- **Spring Batch**: migración de 3 procesos legacy (reporte de transacciones diarias, cálculo de intereses, generación de estados de cuenta anuales) con políticas de skip/retry y reinicio.
- **Patrón BFF**: una fachada HTTPS por canal (web/móvil/cajero), cada una con su propia seguridad.
- **Resiliencia**: Circuit Breaker + Retry (Resilience4j) en todas las llamadas entre microservicios.
- **Seguridad**: API Key (canal-cajero/legacy) + OAuth2.0 (Spring Authorization Server, JWT).
- **Mensajería asíncrona**: Apache Kafka, productor idempotente, patrón afterCommit, Dead Letter Queue, consumidores idempotentes.
- **Contenedores**: cada microservicio dockerizado, orquestados con `docker-compose.yml`.

Ver `instrucciones.md` para los pasos de ejecución y prueba, y `despliegue.md` para el plan de despliegue en la nube (AWS).
