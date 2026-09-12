# Banco XYZ — BFF orientado a microservicios

Patrón **Backend for Frontend (BFF)**: cada canal (Web, Móvil, Cajero) se comunica con un **microservicio de datos** independiente en vez de acceder directamente a la base de datos.

## Estructura 
Web / Móvil / Cajero → BFF (HTTPS) → ms-cuentas (HTTP) → MySQL

## Los 3 BFF

| Canal | Ruta | Expone |
|---|---|---|
| Web | `/api/web/**` | Estado de cuenta completo, transacciones con anomalías, historial de intereses |
| Móvil | `/api/mobile/**` | Saldo y transacciones recientes, payload mínimo |
| Cajero | `/api/cajero/**` | Solo saldo y retiro, con validación de fondos |

## Seguridad

HTTPS + API Key por canal (`X-API-KEY`): `web-2024-xyz-key`, `mobile-2024-xyz-key`, `cajero-2024-xyz-key`.

## Ejecutar

```bash
cd ms-cuentas && mvn spring-boot:run   # puerto 8090
cd bff && mvn spring-boot:run          # puerto 8443, https
```

```bash
curl -k -H "X-API-KEY: web-2024-xyz-key" https://localhost:8443/api/web/cuentas/101/estado-anual
```

## Evidencia

Capturas de los 3 BFF, la comunicación con `ms-cuentas`, el retiro y las pruebas de seguridad (403/409) están en el documento de entrega.

