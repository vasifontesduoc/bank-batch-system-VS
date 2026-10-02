# Bank Batch System — Banco XYZ

Proyecto de migración de procesos batch legacy del Banco XYZ a **Spring Batch**, desarrollado
para la Semana 3 de Desarrollo Backend III (PBY2203) — "Optimizando procesos batch para
mejorar la resiliencia de procesos".

## 1. Objetivo del proyecto

Modernizar tres procesos batch críticos del banco, garantizando integridad y consistencia
de los datos, manejo robusto de errores y buen rendimiento mediante procesamiento
multi-hilo:

1. **Reporte de Transacciones Diarias** — procesa las transacciones del día, descarta
   registros inválidos, detecta anomalías (montos en 0 o sobre el umbral configurado) y
   genera un resumen de la ejecución.
2. **Cálculo de Intereses Mensuales** — aplica la tasa correspondiente según el tipo de
   cuenta (ahorro, préstamo, otros) y actualiza el saldo final en la base de datos.
3. **Generación de Estados de Cuenta Anuales** — compila todos los movimientos del año por
   cuenta y genera un estado de cuenta consolidado, listo para auditorías.

## 2. Estructura del código

```
src/main/java/com/bancoxyz/bank_batch_system/
├── BankBatchSystemApplication.java   # Entry point Spring Boot
├── BatchJobRunner.java               # Lanza un Job específico por --job=...
├── config/
│   ├── BatchConfig.java              # Job/Step: reporte de transacciones diarias
│   └── InteresJobConfig.java         # Job/Step: cálculo de intereses mensuales
├── job/
│   └── CuentaAnualJobConfig.java     # Job (2 steps): estados de cuenta anuales
├── reader/                           # FlatFileItemReader por cada CSV de entrada
├── processor/                        # Validación, normalización y detección de anomalías
├── writer/                           # Persistencia en BD (o respaldo en CSV)
├── tasklet/
│   └── EstadoCuentaAnualTasklet.java # Agrega movimientos por cuenta -> estado anual + informe
├── listener/
│   ├── ResumenTransaccionesListener.java  # Resumen de la ejecución (afterJob)
│   └── TransaccionSkipListener.java       # Traza cada registro descartado
├── exception/                        # Excepciones de negocio (usadas por las skip policies)
├── model/                            # Entidades JPA
└── repository/                       # Spring Data JPA repositories

src/main/resources/
├── input/            # CSVs de entrada (transacciones, intereses, cuentas anuales)
├── output/            # CSVs generados: resúmenes e informes de auditoría
└── application.properties
```

## 3. Origen de los datos

Estructura de datos basada en el dataset legacy de referencia:
https://github.com/KariVillagran/bank_legacy_data

## 4. Decisiones técnicas

### Manejo de errores y tolerancia a fallos
Cada Step usa `.faultTolerant()` con **políticas personalizadas**, no una regla genérica:

- **Errores de datos** (registros incompletos, tipos inválidos, montos nulos o negativos
  donde no corresponde) lanzan una excepción de negocio propia
  (`TransaccionInvalidaException`, `InteresInvalidoException`, `CuentaInvalidaException`,
  todas heredan de `DatoInvalidoException`) y son **saltadas (skip)** hasta un límite
  definido por Step. El dataset real de origen (`bank_legacy_data`) trae una proporción alta
  de registros deliberadamente inválidos (~48% en transacciones e intereses, ~5% en cuentas
  anuales), por lo que el límite se fijó en **600** para transacciones e intereses y **150**
  para cuentas anuales — suficiente margen para procesar el archivo completo sin que el Job
  aborte, sin dejar de ser un límite finito (si superara ese umbral, seguiría siendo señal de
  un problema real en el origen de datos, no solo ruido esperado). Cada registro saltado
  queda registrado en el log por un `SkipListener`.
- **Errores técnicos/transitorios** de acceso a datos (`TransientDataAccessException`, p.
  ej. un timeout momentáneo de conexión a MySQL) se **reintentan** hasta 3 veces con un
  backoff fijo de 1 segundo antes de fallar el chunk, en vez de descartar el registro.

Esta separación evita dos errores comunes: tratar un dato mal formado como si fuera un
problema de infraestructura (reintentarlo no lo arregla), y tratar un problema de
infraestructura como un dato inválido (descartarlo pierde información real).

### Detección de anomalías y resumen (Reporte de Transacciones Diarias)
`TransaccionProcessor` marca como anomalía cualquier transacción con monto `0` o con un
valor absoluto ≥ 5000 (umbral configurable en el código). No se descartan: se registran en
el log con el prefijo `ANOMALÍA` y se cuentan aparte. Al finalizar el Job,
`ResumenTransaccionesListener` imprime en consola y escribe en
`output/resumen_transacciones_<timestamp>.csv` cuántos registros se procesaron, cuántas
anomalías se detectaron y cuántos se rechazaron.

### Informe de auditoría (Estados de Cuenta Anuales)
El Job `estadosCuentaAnualesJob` tiene **dos Steps**:
1. `cuentaAnualStep` valida y guarda cada movimiento individual.
2. `compilarEstadoAnualStep` (Tasklet) agrupa todos los movimientos por cuenta, calcula
   totales de depósitos/retiros/otros y el saldo final, los guarda en la tabla
   `estados_cuenta_anuales` y genera `output/informe_auditoria_anual_<timestamp>.csv`.

### Escalamiento
Se optó por **procesamiento multi-hilo** (`TaskExecutor` en cada Step) en vez de
particiones por rango, porque el trabajo es I/O-bound (lectura de CSV + escritura a BD) y
los archivos no se prestan naturalmente a particionarse por rangos independientes. El pool
se configuró con **3 hilos núcleo / 5 máximo** y una cola de 10 chunks, buscando paralelismo
sin saturar el pool de conexiones de MySQL.

### Rutas externas configurables (output y respaldos)
Los resultados generados en cada ejecución (resumen de transacciones, informe de auditoría
anual y respaldo CSV) **no se escriben dentro de `src/main/resources`**, porque esa carpeta
forma parte de los recursos empaquetados de la aplicación y complica la administración de
resultados al desplegar en otros entornos. En su lugar, las rutas se leen desde
`application.properties` mediante `@Value` y son parametrizables por ambiente:

```properties
batch.output.dir=./data/output
batch.backup.dir=./data/backup
```

Se pueden sobreescribir sin tocar el código, por ejemplo:
```bash
./mvnw spring-boot:run -Dspring-boot.run.arguments="--job=anual --batch.output.dir=/var/data/banco-xyz/output"
```
Ambas carpetas se crean automáticamente si no existen y están excluidas del control de
versiones (`.gitignore`), ya que son artefactos de ejecución, no código fuente.

## 5. Cómo ejecutar el proyecto

### Requisitos previos
- Java 17+
- Maven (o usar el wrapper `./mvnw` incluido)
- MySQL corriendo localmente

### Configuración de la base de datos
```sql
CREATE DATABASE banco_xyz;
```
Ajusta usuario/contraseña en `src/main/resources/application.properties` si no usas
`root/password`. Las tablas se crean automáticamente (`spring.jpa.hibernate.ddl-auto=update`)
y el esquema de metadatos de Spring Batch también (`spring.batch.jdbc.initialize-schema=always`).

### Compilar
```bash
./mvnw clean package
```

### Ejecutar cada Job (uno a la vez, para poder capturar la evidencia de cada uno)
```bash
./mvnw spring-boot:run -Dspring-boot.run.arguments=--job=transacciones
./mvnw spring-boot:run -Dspring-boot.run.arguments=--job=intereses
./mvnw spring-boot:run -Dspring-boot.run.arguments=--job=anual
```

Cada corrida queda registrada en la consola (con el resumen o informe correspondiente) y,
para los procesos de transacciones y cuentas anuales, además genera un CSV en
`./data/output/` (carpeta configurable en `application.properties`, fuera del jar
empaquetado — ver sección 4, "Rutas externas configurables").

## 6. Evidencia de ejecución

> Completa esta sección con capturas de pantalla o el output de consola de cada Job antes
> de entregar, junto con los CSV generados en `src/main/resources/output/`.

- [ ] Captura de `reporteTransaccionesJob` (consola + `data/output/resumen_transacciones_*.csv`)
- [ ] Captura de `calculoInteresesJob` (consola)
- [ ] Captura de `estadosCuentaAnualesJob` (consola + `data/output/informe_auditoria_anual_*.csv`)
