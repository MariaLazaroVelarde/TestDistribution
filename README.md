# TestDistribution - Microservicio de Distribución de Agua

## 📋 Descripción del Proyecto

**TestDistribution** es un microservicio desarrollado con **Spring Boot 3.4.5** y **Java 17** que forma parte de un sistema integral de gestión de JASS (Juntas Administradoras de Servicios de Saneamiento).

## 🧪 Implementación de Pruebas

### 1. Pruebas Parametrizadas por Participantes

Se implementaron pruebas parametrizadas que validan el comportamiento del sistema con diferentes tipos de usuarios:

#### Tipos de Participantes Soportados

- **ADMINISTRADORES**: Pueden crear, modificar y eliminar programas
- **OPERADORES**: Pueden crear y modificar programas, pero no eliminar
- **TÉCNICOS**: Pueden crear programas y actualizar su estado
- **SUPERVISORES**: Pueden ver todos los programas y aprobar cambios
- **CLIENTES**: Acceso limitado de solo lectura

#### Casos de Prueba Implementados

1. **Validación de Permisos por Participante**
2. **Validación de Estados de Programa**
3. **Validación de Horarios por Zona**
4. **Validación de Tipos de Tarifa**
5. **Validación de Fechas por Día de Semana**

### 2. Cobertura de Código con JaCoCo

#### Umbrales de Cobertura Configurados

| Métrica | Umbral Mínimo |
|---------|---------------|
| Instructions | 70% |
| Branches | 60% |
| Lines | 70% |
| Methods | 70% |
| Classes | 70% |

#### Comandos para Generar Reportes

```bash
# Ejecutar pruebas y generar reporte de cobertura
mvn clean test jacoco:report

# Verificar umbrales de cobertura
mvn jacoco:check

# Ver reporte HTML
open target/site/jacoco/index.html
```

### 3. Análisis con SonarQube

#### Hallazgos Corregidos

1. **Duplicación de Código**: Mensajes de error centralizados
2. **Métodos Largos**: Refactorización en métodos más pequeños
3. **Constantes Mágicas**: Extracción a constantes
4. **Manejo de Excepciones**: Mejora en el manejo de errores

### 4. GitHub Actions CI/CD

#### Pipeline Implementado

El pipeline de CI/CD incluye los siguientes jobs:

1. **Code Quality Analysis**: Análisis estático con SonarQube
2. **Unit Tests**: Ejecución de pruebas unitarias
3. **Parametrized Tests**: Ejecución de pruebas parametrizadas
4. **Coverage Analysis**: Generación de reportes JaCoCo
5. **Build Application**: Compilación del proyecto
6. **Security Scan**: Análisis OWASP Dependency Check
7. **Deploy to Staging**: Despliegue automático
8. **Notify Results**: Notificaciones de resultados

## 🚀 Instalación y Configuración

### Prerrequisitos

- Java 17+
- Maven 3.9+
- MongoDB 4.4+
- Git

### Instalación Local

```bash
# Clonar el repositorio
git clone https://github.com/vallegrande/test-distribution.git
cd test-distribution

# Compilar el proyecto
mvn clean compile

# Ejecutar pruebas
mvn test

# Generar reporte de cobertura
mvn jacoco:report

# Ejecutar la aplicación
mvn spring-boot:run
```

## 🧪 Ejecución de Pruebas

### Pruebas Unitarias

```bash
# Ejecutar todas las pruebas
mvn test

# Ejecutar pruebas específicas
mvn test -Dtest=DistributionProgramServiceImplTest

# Ejecutar pruebas parametrizadas
mvn test -Dtest=ParametrizedDistributionProgramTest
```

### Pruebas con Cobertura

```bash
# Ejecutar pruebas con cobertura
mvn clean test jacoco:report

# Verificar umbrales
mvn jacoco:check

# Ver reporte HTML
open target/site/jacoco/index.html
```

## 🔧 Comandos Útiles

```bash
# Desarrollo
mvn spring-boot:run                    # Ejecutar aplicación
mvn clean compile                      # Compilar proyecto
mvn clean package                      # Empaquetar aplicación

# Testing
mvn test                              # Ejecutar pruebas
mvn jacoco:report                     # Generar reporte de cobertura

# Calidad
mvn sonar:sonar                       # Análisis SonarQube
mvn dependency:check                  # Verificar dependencias
```

## 📝 Contribución

### Flujo de Trabajo

1. **Fork** del repositorio
2. **Crear branch** para feature: `git checkout -b feature/nueva-funcionalidad`
3. **Commit** cambios: `git commit -m 'Add nueva funcionalidad'`
4. **Push** al branch: `git push origin feature/nueva-funcionalidad`
5. **Crear Pull Request`

### Estándares de Código

- Seguir convenciones de Java
- Cobertura de pruebas > 70%
- Sin duplicación de código
- Documentación actualizada
- Análisis SonarQube sin issues críticos

---

**Última actualización**: Diciembre 2024
**Versión**: 1.0.0
**Estado**: ✅ Activo
