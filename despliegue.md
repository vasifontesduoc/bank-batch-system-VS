# Plan de despliegue en la nube (AWS) — EFT S9

Este documento describe cómo llevar el ecosistema de microservicios (actualmente orquestado con Docker Compose en local) a un entorno de nube en AWS, preparado para producción.

## 1. Mapeo de componentes locales → servicios AWS

| Componente local (Docker Compose) | Servicio AWS equivalente | Justificación |
|---|---|---|
| Contenedores de cada microservicio/BFF | **Amazon ECS (Fargate)** | Orquestación de contenedores sin gestionar servidores; cada microservicio se despliega como una Task Definition independiente, escalable por separado. |
| `mysql` | **Amazon RDS for MySQL** | Base de datos administrada, con backups automáticos, alta disponibilidad (Multi-AZ) y parches gestionados. |
| `kafka` | **Amazon MSK (Managed Streaming for Kafka)** | Cluster Kafka administrado, con replicación y monitoreo integrados. |
| Imágenes Docker (`docker build`) | **Amazon ECR (Elastic Container Registry)** | Registro privado de imágenes; cada `docker build` local se reemplaza por `docker push` a ECR. |
| `eureka-server` | Contenedor en ECS (igual que hoy) | Spring Cloud Netflix Eureka no tiene equivalente nativo AWS; se mantiene como servicio propio, con Service Discovery de AWS Cloud Map como alternativa a futuro. |
| `config-server` | Contenedor en ECS + **AWS Systems Manager Parameter Store** o **AWS Secrets Manager** | El `config-repo` puede migrar a Parameter Store para configuración sensible (contraseñas de BD, client secrets OAuth2), evitando credenciales en texto plano en el repo. |
| Puertos expuestos de los BFFs (8443-8445) | **Application Load Balancer (ALB)** + **Amazon Route 53** | Un ALB por canal (o rutas por path) distribuye tráfico HTTPS hacia los BFFs; Route 53 gestiona el dominio público. |
| Certificados SSL autofirmados (`keystore.p12`) | **AWS Certificate Manager (ACM)** | Certificados TLS válidos y renovados automáticamente, terminación SSL en el ALB. |

## 2. Pasos de despliegue

1. **Construir y publicar las imágenes en ECR**
```bash
   aws ecr create-repository --repository-name bank-xyz/ms-cuentas
   docker build -t bank-xyz/ms-cuentas ./ms-cuentas
   docker tag bank-xyz/ms-cuentas:latest <account-id>.dkr.ecr.<region>.amazonaws.com/bank-xyz/ms-cuentas:latest
   docker push <account-id>.dkr.ecr.<region>.amazonaws.com/bank-xyz/ms-cuentas:latest
```
   Repetir para cada microservicio/BFF/servidor de infraestructura.

2. **Provisionar la base de datos**
   Crear una instancia RDS MySQL 8.0, en una subred privada, y migrar el esquema `banco_xyz` (mismo DDL que genera Hibernate localmente, o un script de migración versionado con Flyway/Liquibase para producción).

3. **Provisionar el cluster Kafka (MSK)**
   Crear un cluster MSK con el mismo número de particiones/tópicos usados hoy (`retiros-topic`, `pagos-topic`, y sus `.DLT`), y actualizar `SPRING_KAFKA_BOOTSTRAP_SERVERS` a los brokers de MSK.

4. **Definir las Task Definitions de ECS**
   Cada servicio de `docker-compose.yml` se traduce a una Task Definition de ECS (CPU/memoria, variables de entorno —las mismas que hoy usa `docker-compose.yml`—, y el rol IAM con permisos mínimos necesarios).

5. **Configurar los Services de ECS**
   Un `ECS Service` por microservicio, con un `desired count` ≥ 2 para alta disponibilidad, y Auto Scaling basado en CPU/memoria.

6. **Configurar networking y seguridad**
   - VPC con subredes públicas (ALB) y privadas (microservicios, RDS, MSK).
   - Security Groups restringiendo el tráfico: solo el ALB puede llegar a los BFFs; solo los microservicios internos pueden llegar a RDS/MSK.
   - Secrets (contraseñas DB, client-secret OAuth2) en **AWS Secrets Manager**, inyectados como variables de entorno en las Task Definitions (no quedan en el código ni en `config-repo`).

7. **Exponer los BFFs al público**
   Application Load Balancer con certificado ACM, enrutando `/web/*`, `/mobile/*`, `/cajero/*` (o subdominios) hacia los target groups de cada BFF. Los microservicios internos (`ms-cuentas`, `ms-pagos`, `ms-clientes`, `ms-fraude`, `ms-notificaciones`) nunca se exponen directamente a Internet — igual que hoy en Docker Compose, donde no publican puertos al host.

8. **CI/CD**
   Pipeline (GitHub Actions o AWS CodePipeline) que en cada push a `main`: compila y testea cada módulo Maven, construye y publica la imagen a ECR, y actualiza el servicio ECS correspondiente (`aws ecs update-service --force-new-deployment`).

## 3. Observabilidad en producción

- **Amazon CloudWatch Logs**: cada contenedor ECS envía sus logs (reemplaza `docker compose logs`).
- **CloudWatch Alarms** sobre métricas de Resilience4j (tasa de apertura de Circuit Breaker) y profundidad de las colas Kafka, para alertar antes de que un microservicio caído afecte a los demás.
- **AWS X-Ray** (opcional) para trazabilidad distribuida entre `bff-* → ms-pagos → ms-cuentas`.

## 4. Consideraciones de costo/escalado

Los microservicios de solo-lectura o baja carga (`ms-clientes`) pueden correr con 1 réplica mínima; `ms-cuentas` y `ms-pagos`, al ser el punto central de escritura, deben escalar horizontalmente según carga. El Auto Scaling de ECS se configura sobre la métrica de CPU y, idealmente, sobre el tamaño de la cola de Kafka pendiente por consumir.

## Gestión de la clave interna entre microservicios

En local, `internal.api.key` vive en `config-server/config-repo/application.properties`. En AWS no debe versionarse: se guarda en AWS Secrets Manager (o SSM Parameter Store) y se inyecta como variable de entorno `INTERNAL_API_KEY` en las task definitions de ECS de ms-cuentas, ms-pagos, ms-clientes y los tres BFF. Se recomienda rotarla periódicamente y, a futuro, reemplazarla por JWT por servicio o mTLS.
