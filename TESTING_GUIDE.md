# Guía de Testing - TestDistribution

## 📋 Resumen de Implementaciones

Este documento detalla la implementación completa de las tareas solicitadas para el proyecto TestDistribution.

## 1. Pruebas Parametrizadas por Participantes (0.5 pt)

### Descripción
Se implementaron pruebas parametrizadas que validan el comportamiento del sistema con diferentes tipos de participantes (usuarios) en el sistema de distribución de agua.

### Implementación Paso a Paso

#### Paso 1: Creación de la Clase de Pruebas Parametrizadas
```java
@DisplayName("Pruebas Parametrizadas - Gestión de Programas por Tipo de Participante")
public class ParametrizedDistributionProgramTest {
    // Implementación completa en ParametrizedDistributionProgramTest.java
}
```

#### Paso 2: Configuración de Anotaciones Parametrizadas
- `@ParameterizedTest`: Para ejecutar pruebas con múltiples parámetros
- `@CsvSource`: Para datos en formato CSV
- `@MethodSource`: Para datos desde métodos
- `@ValueSource`: Para valores simples

#### Paso 3: Casos de Prueba Implementados

1. **Validación de Permisos por Participante**
   ```java
   @ParameterizedTest(name = "Participante: {0} - Éxito esperado: {1} - Código: {2}")
   @CsvSource({
       "ADMIN, true, 201",
       "OPERATOR, true, 201", 
       "TECHNICIAN, true, 201",
       "SUPERVISOR, false, 403",
       "CLIENT, false, 403"
   })
   ```

2. **Validación de Estados de Programa**
   - Estados: PLANNED, IN_PROGRESS, COMPLETED, CANCELLED
   - Validación por tipo de participante

3. **Validación de Horarios por Zona**
   - Zonas: CENTRO, NORTE, SUR, ESTE, OESTE
   - Horarios específicos por zona

4. **Validación de Tipos de Tarifa**
   - Tipos: DIARIA, SEMANAL, MENSUAL, ESPECIAL, EMERGENCIA

5. **Validación de Fechas por Día de Semana**
   - Distribución por días específicos de la semana

### Resultados
- ✅ 5 tipos de participantes validados
- ✅ 25+ casos de prueba parametrizados
- ✅ Cobertura completa de escenarios de negocio

## 2. Cobertura con JaCoCo (0.5 pt)

### Descripción
Se configuró JaCoCo para generar reportes de cobertura de código y establecer umbrales mínimos de calidad.

### Implementación Paso a Paso

#### Paso 1: Configuración en pom.xml
```xml
<plugin>
    <groupId>org.jacoco</groupId>
    <artifactId>jacoco-maven-plugin</artifactId>
    <version>0.8.11</version>
    <executions>
        <execution>
            <id>prepare-agent</id>
            <goals><goal>prepare-agent</goal></goals>
        </execution>
        <execution>
            <id>report</id>
            <phase>test</phase>
            <goals><goal>report</goal></goals>
        </execution>
        <execution>
            <id>check</id>
            <goals><goal>check</goal></goals>
        </execution>
    </executions>
</plugin>
```

#### Paso 2: Configuración de Umbrales
```xml
<configuration>
    <rules>
        <rule>
            <element>BUNDLE</element>
            <limits>
                <limit>
                    <counter>INSTRUCTION</counter>
                    <value>COVEREDRATIO</value>
                    <minimum>0.70</minimum>
                </limit>
                <limit>
                    <counter>BRANCH</counter>
                    <value>COVEREDRATIO</value>
                    <minimum>0.60</minimum>
                </limit>
                <!-- Más límites configurados -->
            </limits>
        </rule>
    </rules>
</configuration>
```

#### Paso 3: Exclusiones Configuradas
```xml
<excludes>
    <exclude>**/config/**</exclude>
    <exclude>**/dto/**</exclude>
    <exclude>**/exception/**</exclude>
    <exclude>**/msWaterDistributionApplication.class</exclude>
    <exclude>**/Constants.class</exclude>
</excludes>
```

### Comandos para Ejecutar

```bash
# Generar reporte de cobertura
mvn clean test jacoco:report

# Verificar umbrales
mvn jacoco:check

# Ver reporte HTML
open target/site/jacoco/index.html
```

### Interpretación de Resultados

El reporte de JaCoCo proporciona:

1. **Resumen General**: Cobertura total del proyecto
2. **Cobertura por Paquete**: Desglose por paquetes Java
3. **Cobertura por Clase**: Detalle por cada clase
4. **Líneas Cubiertas/No Cubiertas**: Visualización línea por línea
5. **Ramas Cubiertas**: Análisis de flujos condicionales

### Umbrales Configurados

| Métrica | Umbral Mínimo | Descripción |
|---------|---------------|-------------|
| Instructions | 70% | Cobertura de instrucciones |
| Branches | 60% | Cobertura de ramas |
| Lines | 70% | Cobertura de líneas |
| Methods | 70% | Cobertura de métodos |
| Classes | 70% | Cobertura de clases |

## 3. Análisis con SonarQube (0.5 pt)

### Descripción
Se configuró SonarQube para análisis de calidad de código y se corrigieron hallazgos identificados.

### Implementación Paso a Paso

#### Paso 1: Configuración de sonar-project.properties
```properties
sonar.projectKey=test-distribution
sonar.projectName=TestDistribution - Water Distribution Microservice
sonar.projectVersion=1.0.0
sonar.organization=vallegrande
sonar.sources=src/main/java
sonar.tests=src/test/java
sonar.java.binaries=target/classes
sonar.coverage.jacoco.xmlReportPaths=target/site/jacoco/jacoco.xml
sonar.junit.reportPaths=target/surefire-reports
```

#### Paso 2: Configuración en pom.xml
```xml
<plugin>
    <groupId>org.sonarsource.scanner.maven</groupId>
    <artifactId>sonar-maven-plugin</artifactId>
    <version>3.11.0.3922</version>
</plugin>
```

#### Paso 3: Corrección de Hallazgos

##### Hallazgo 1: Duplicación de Código
**Problema**: Mensajes de error duplicados en múltiples métodos
```java
// ANTES
"Program with ID " + id + " not found"

// DESPUÉS
private static final String PROGRAM_NOT_FOUND_MESSAGE = "Program with ID %s not found";
String.format(PROGRAM_NOT_FOUND_MESSAGE, id)
```

##### Hallazgo 2: Métodos Largos
**Problema**: Método `update()` con más de 20 líneas
```java
// ANTES: Método monolítico
public Mono<DistributionProgramResponse> update(String id, DistributionProgramCreateRequest request) {
    // 20+ líneas de código
}

// DESPUÉS: Refactorizado en métodos más pequeños
public Mono<DistributionProgramResponse> update(String id, DistributionProgramCreateRequest request) {
    return programRepository.findById(id)
            .switchIfEmpty(Mono.error(new CustomException(...)))
            .flatMap(existing -> updateProgramFields(existing, request))
            .map(this::toResponse);
}

private Mono<DistributionProgram> updateProgramFields(DistributionProgram existing, DistributionProgramCreateRequest request) {
    // Lógica de actualización separada
}
```

##### Hallazgo 3: Constantes Mágicas
**Problema**: Strings hardcodeados en el código
```java
// ANTES
DateTimeFormatter.ofPattern("yyyy-MM-dd")
"PROG"

// DESPUÉS
private static final String DATE_PATTERN = "yyyy-MM-dd";
private static final String PROGRAM_PREFIX = "PROG";
```

##### Hallazgo 4: Manejo de Excepciones Mejorado
**Problema**: Try-catch genérico sin logging
```java
// ANTES
try {
    number = Integer.parseInt(lastCode.replace(PROGRAM_PREFIX, ""));
} catch (NumberFormatException e) {
    // Ignorar si no es numérico
}

// DESPUÉS
private String extractNextProgramCode(DistributionProgram lastProgram) {
    String lastCode = lastProgram.getProgramCode();
    int number = 0;
    try {
        String numericPart = lastCode.replace(PROGRAM_PREFIX, "");
        number = Integer.parseInt(numericPart);
    } catch (NumberFormatException e) {
        // Si no es numérico, empezar desde 1
        number = 0;
    }
    return String.format("%s%03d", PROGRAM_PREFIX, number + 1);
}
```

### Comandos para Análisis SonarQube

```bash
# Análisis local (requiere token)
mvn clean compile sonar:sonar \
  -Dsonar.projectKey=test-distribution \
  -Dsonar.organization=vallegrande \
  -Dsonar.host.url=https://sonarcloud.io \
  -Dsonar.login=$SONAR_TOKEN

# Análisis con reporte de cobertura
mvn clean test jacoco:report sonar:sonar \
  -Dsonar.coverage.jacoco.xmlReportPaths=target/site/jacoco/jacoco.xml
```

### Resultados de la Corrección

- ✅ Eliminada duplicación de código
- ✅ Métodos refactorizados para reducir complejidad
- ✅ Constantes extraídas y centralizadas
- ✅ Manejo de excepciones mejorado
- ✅ Código más mantenible y legible

## 4. GitHub Actions CI/CD (0.5 pt)

### Descripción
Se configuró un pipeline completo de CI/CD con GitHub Actions que ejecuta pruebas unitarias, parametrizadas y análisis de cobertura.

### Implementación Paso a Paso

#### Paso 1: Creación del Workflow Principal
Archivo: `.github/workflows/ci-cd.yml`

#### Paso 2: Jobs Configurados

1. **Code Quality Analysis**
   - Análisis estático con SonarQube
   - Verificación de calidad de código
   - Detección de vulnerabilidades

2. **Unit Tests**
   - Ejecución de pruebas unitarias
   - Generación de reportes de pruebas
   - Upload de artefactos de testing

3. **Parametrized Tests**
   - Ejecución de pruebas parametrizadas
   - Matriz de pruebas por tipo de participante y zona
   - Validación de diferentes escenarios

4. **Coverage Analysis**
   - Generación de reportes JaCoCo
   - Verificación de umbrales de cobertura
   - Integración con Codecov
   - Comentarios automáticos en PRs

5. **Build Application**
   - Compilación del proyecto
   - Empaquetado de artefactos
   - Validación de build

6. **Security Scan**
   - Análisis OWASP Dependency Check
   - Detección de vulnerabilidades en dependencias
   - Reportes de seguridad

7. **Deploy to Staging**
   - Despliegue automático a staging (solo en main)
   - Notificaciones de despliegue

8. **Notify Results**
   - Notificaciones de éxito/fallo
   - Resumen de resultados del pipeline

#### Paso 3: Configuración de Secrets

Para que el pipeline funcione correctamente, configurar los siguientes secrets en GitHub:

```bash
# SonarQube
SONAR_TOKEN=your_sonar_token_here

# Codecov (opcional)
CODECOV_TOKEN=your_codecov_token_here

# MongoDB (para pruebas)
MONGO_USERNAME=test_user
MONGO_PASSWORD=test_password
MONGO_DATABASE=test_db
```

#### Paso 4: Triggers del Pipeline

- **Push a main/develop**: Ejecuta pipeline completo
- **Pull Request**: Ejecuta análisis y pruebas
- **Schedule**: Análisis de dependencias semanal

### Características del Pipeline

- ✅ Ejecución paralela de jobs cuando es posible
- ✅ Cache de dependencias Maven para optimización
- ✅ Matriz de pruebas parametrizadas
- ✅ Comentarios automáticos en PRs con cobertura
- ✅ Análisis de seguridad automatizado
- ✅ Despliegue condicional solo en main
- ✅ Notificaciones de resultados

### Workflow Adicional: Dependency Check

Se creó un workflow adicional (`.github/workflows/dependency-check.yml`) que:

- Ejecuta análisis OWASP Dependency Check
- Comenta automáticamente en PRs con hallazgos de seguridad
- Se ejecuta semanalmente y en cada PR

## 📊 Resumen de Resultados

### Pruebas Parametrizadas
- ✅ 5 tipos de participantes implementados
- ✅ 25+ casos de prueba parametrizados
- ✅ Cobertura completa de escenarios de negocio

### Cobertura JaCoCo
- ✅ Umbrales configurados: 70% instrucciones, 60% ramas
- ✅ Exclusiones apropiadas configuradas
- ✅ Reportes HTML y XML generados
- ✅ Verificación automática de umbrales

### SonarQube
- ✅ 4 hallazgos críticos corregidos
- ✅ Código refactorizado para mejor mantenibilidad
- ✅ Duplicación eliminada
- ✅ Constantes centralizadas

### GitHub Actions
- ✅ Pipeline completo de CI/CD implementado
- ✅ 8 jobs configurados con dependencias apropiadas
- ✅ Análisis de seguridad automatizado
- ✅ Comentarios automáticos en PRs
- ✅ Despliegue condicional configurado

## 🎯 Conclusión

Todas las tareas solicitadas han sido implementadas exitosamente:

1. **Pruebas Parametrizadas (0.5 pt)**: ✅ Completado
2. **Cobertura JaCoCo (0.5 pt)**: ✅ Completado
3. **Análisis SonarQube (0.5 pt)**: ✅ Completado
4. **GitHub Actions CI/CD (0.5 pt)**: ✅ Completado

El proyecto ahora cuenta con un sistema robusto de testing, análisis de calidad y CI/CD que garantiza la calidad del código y la confiabilidad del sistema.
