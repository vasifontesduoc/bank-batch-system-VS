# Instrucciones de ejecución y prueba — EFT S9

Guía paso a paso para levantar el sistema completo y probar cada componente.

## Prerrequisitos

- Docker Desktop instalado y corriendo.
- Java 17 y Maven (solo si quieres compilar/correr algo fuera de Docker).
- Un cliente HTTP (curl, Postman) para las pruebas.

## 1. Clonar el repositorio

```bash
git clone https://github.com/vasifontesduoc/bank-batch-system-VS.git
cd bank-batch-system-VS
```

## 2. Levantar todo el ecosistema con Docker Compose

```bash
docker compose build
docker compose up -d
docker compose ps
```

Esto levanta: MySQL, Kafka, Eureka, Config Server, Auth Server, y los 6 microservicios (`ms-cuentas`, `ms-pagos`, `ms-clientes`, `ms-fraude`, `ms-notificaciones`) + los 3 BFFs.

Verifica que todos los servicios se registraron en Eureka abriendo en el navegador:
**http://localhost:8761**

Deberías ver: BFF-WEB, BFF-MOBILE, BFF-CAJERO, MS-CUENTAS, MS-PAGOS, MS-CLIENTES, MS-FRAUDE, MS-NOTIFICACIONES.

## 3. Probar la seguridad (API Key + OAuth2.0)

Obtener un token OAuth2.0 (client_credentials):

```bash
curl -s -X POST http://localhost:9000/oauth2/token \
  -u bank-client:bank-secret \
  -d "grant_type=client_credentials&scope=api.access"
```

Usar el `access_token` recibido para llamar al BFF web (HTTPS, certificado autofirmado):

```bash
TOKEN="<pega aqui el access_token>"
curl -k -s https://localhost:8443/api/cuentas/1/estado \
  -H "Authorization: Bearer $TOKEN"
```

## 4. Probar los microservicios nuevos (ms-pagos, ms-clientes)

Estos microservicios no exponen puerto al host (solo son accesibles dentro de la red Docker, como el resto de microservicios internos). Para probarlos directamente:

```bash
# Crear un cliente
docker exec -it ms-cuentas curl -s -X POST http://ms-clientes:8095/internal/clientes \
  -H "Content-Type: application/json" \
  -d '{"nombre":"Juan Perez","email":"juan@example.com","telefono":"+56911111111","direccion":"Santiago","cuentaId":1}'

# Consultar resumen financiero del cliente (llamada resiliente a ms-cuentas)
docker exec -it ms-cuentas curl -s http://ms-clientes:8095/internal/clientes/1/resumen-financiero

# Procesar un pago (debita la cuenta origen vía ms-cuentas + publica evento Kafka)
docker exec -it ms-cuentas curl -s -X POST http://ms-pagos:8094/internal/pagos \
  -H "Content-Type: application/json" \
  -d '{"cuentaOrigenId":1,"monto":500,"descripcion":"Pago de servicio"}'
```

Revisa los logs de `ms-notificaciones` para confirmar que recibió el evento:

```bash
docker compose logs ms-notificaciones --tail=20
```

## 5. Probar la resiliencia (Circuit Breaker)

Para ver el Circuit Breaker en acción, detén `ms-cuentas` y vuelve a llamar al resumen financiero de un cliente:

```bash
docker compose stop ms-cuentas
docker exec -it ms-pagos curl -s http://ms-clientes:8095/internal/clientes/1/resumen-financiero
```

Debería responder con `"disponible":false` y un mensaje de fallback, en vez de fallar o colgarse. Luego reactiva `ms-cuentas`:

```bash
docker compose start ms-cuentas
```

## 6. Ejecutar los Jobs de Spring Batch

Los 3 procesos legacy migrados viven en el módulo `batch-jobs`. Requieren que MySQL esté corriendo (ya está, si seguiste el paso 2):

```bash
cd batch-jobs
mvn clean package -DskipTests

java -jar target/batch-jobs-0.0.1-SNAPSHOT.jar --spring.batch.job.name=reporteTransaccionesDiariasJob runId="$(date +%s)"
java -jar target/batch-jobs-0.0.1-SNAPSHOT.jar --spring.batch.job.name=calculoInteresesJob runId="$(date +%s)"
java -jar target/batch-jobs-0.0.1-SNAPSHOT.jar --spring.batch.job.name=generacionEstadosCuentaAnualesJob runId="$(date +%s)"
```

Cada ejecución debe terminar con status `COMPLETED` en los logs. Puedes verificar los resultados en MySQL:

```bash
docker exec -it mysql mysql -uroot -ppassword banco_xyz -e "
SELECT COUNT(*) FROM reporte_transacciones_diarias;
SELECT COUNT(*) FROM intereses_calculados;
SELECT COUNT(*) FROM estados_cuenta_anuales;
"
```

## 7. Apagar el ecosistema

```bash
cd ..
docker compose down
```
