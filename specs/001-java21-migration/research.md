# Research: Migración Java 8 → Java 21 (bad-inventory-api)

**Feature**: `001-java21-migration`
**Generated**: 2026-05-14
**Purpose**: Resolver todos los NEEDS CLARIFICATION del Technical Context y documentar
decisiones técnicas con su justificación.

---

## Hallazgo 0 — Inventario Real del Código Fuente

Antes de investigar dependencias externas, se realizó un análisis directo del
repositorio para entender el alcance real de la migración.

| Componente | Estado actual | Acción de migración |
|-----------|--------------|---------------------|
| `pom.xml` | Spring Boot 2.7.18 / Java 1.8 / packaging jar | Actualizar versiones, cambiar a war |
| `BadInventoryApiApplication.java` | `@SpringBootApplication` estándar | Extender `SpringBootServletInitializer` |
| `GodController.java` | 1 import `javax.annotation.PostConstruct` | Cambiar a `jakarta.annotation.PostConstruct` |
| `application.properties` | Propiedades Spring Boot 2.x estándar | Verificar compatibilidad (sin cambios necesarios) |
| **Suite de pruebas** | **NO EXISTE** | **Crear SmokeTest mínimo (gate de paridad)** |

**Impacto total de la migración: 3 archivos modificados + 1 archivo nuevo (SmokeTest)**

---

## Decisión 1 — Spring Boot 2.7.18 → 3.3.x: Breaking Changes Aplicables

**Decisión**: Actualizar a Spring Boot **3.3.5** (última versión estable de la línea
3.3.x en Maven Central al 2026-05-14).

**Justificación**: La línea 3.3.x es la más reciente de generación 3 con soporte
activo. Spring Boot 3.x requiere Java 17 como mínimo; Java 21 es plenamente compatible.

**Breaking Changes que aplican a este proyecto:**

| Cambio | Impacto en bad-inventory-api | Resolución |
|--------|------------------------------|------------|
| Namespace Jakarta EE 10 (`javax.*` → `jakarta.*`) | `GodController.java` usa `javax.annotation.PostConstruct` | Reemplazar import |
| BOM de H2 pasa de 1.4.x → 2.2.x | Posibles diferencias de comportamiento SQL en DDL inline | Verificado: DDL usa SQL estándar; compatible sin cambios |
| `spring.security.*` habilitado por defecto | **N/A** — no hay `spring-boot-starter-security` en pom.xml | No aplica |
| Actuator: `management.endpoints.web.exposure.include=*` | Sin Spring Security, el comportamiento es idéntico al de SBoot 2.x (todos los endpoints expuestos) | Sin cambio en `application.properties` |
| `@ConstructorBinding` eliminado de Spring Boot 3.x | **N/A** — no se usa en este proyecto | No aplica |
| Hibernate 6.x (para JPA) | **N/A** — el proyecto usa `spring-boot-starter-jdbc`, no JPA/Hibernate | No aplica |

**Alternativas consideradas**:
- Spring Boot 3.0.x → rechazado: versión EOL.
- Spring Boot 3.2.x → rechazado: línea anterior, sin soporte a largo plazo vs 3.3.x.

---

## Decisión 2 — `javax.annotation.PostConstruct` → `jakarta.annotation.PostConstruct`

**Decisión**: Reemplazar el import directamente en `GodController.java`. No se
requiere dependencia explícita adicional.

**Justificación**: Spring Boot 3.x incluye `jakarta.annotation-api` 2.x de forma
transitiva a través de `spring-boot-starter`. El import correcto es:

```java
// ANTES (Java EE 8 / javax):
import javax.annotation.PostConstruct;

// DESPUÉS (Jakarta EE 10 / jakarta):
import jakarta.annotation.PostConstruct;
```

**Confirmación**: `@PostConstruct` existe en `jakarta.annotation.PostConstruct`
desde Jakarta Annotations 2.0 (incluida en Spring Boot 3.x BOM).

**Alternativas consideradas**:
- Eliminar `@PostConstruct` y usar `CommandLineRunner` → rechazado: cambio de
  comportamiento (Principio I); además introduce lógica que no existe en la versión Java 8.

---

## Decisión 3 — Driver H2: Compatibilidad 1.4.x → 2.2.x

**Decisión**: Dejar la gestión de la versión de H2 al BOM de Spring Boot 3.3.x
(H2 2.2.x). No declarar la versión explícitamente en pom.xml.

**Justificación**: Spring Boot 3.x gestiona H2 2.2.x en su BOM. Esta versión
introduce cambios en el parser SQL, pero el DDL inline de `GodController.setupDb()`
usa únicamente sintaxis SQL estándar:

```sql
-- Estos statements son compatibles con H2 2.x sin modificación:
CREATE TABLE IF NOT EXISTS products (id INT AUTO_INCREMENT PRIMARY KEY,
  title VARCHAR(100), price DOUBLE, description VARCHAR(500), category VARCHAR(100))
INSERT INTO products (...) VALUES (...)
SELECT * FROM products
SELECT * FROM products WHERE id = ...
```

**Riesgo residual**: `DOUBLE` es un alias válido en H2 2.x (se mapea a `DOUBLE PRECISION`).
Comportamiento idéntico al de H2 1.4.x para este esquema.

**Cambio en URL de conexión**: La URL `jdbc:h2:mem:testdb;DB_CLOSE_DELAY=-1;DB_CLOSE_ON_EXIT=FALSE`
es compatible con H2 2.x sin modificación.

**Alternativas consideradas**:
- Pin explícito a H2 1.4.200 → rechazado: incompatible con el módulo system de Java 21
  (`IllegalAccessError` en módulos de encapsulación fuerte activados por defecto en Java 17+).

---

## Decisión 4 — SpringBootServletInitializer para Tomcat Externo

**Decisión**: Modificar `BadInventoryApiApplication.java` para extender
`SpringBootServletInitializer`. Cambiar packaging a `war`. Añadir
`spring-boot-starter-tomcat` con scope `provided`.

**Justificación**: Requerimiento explícito FR-004 y US2. Patrón estándar de
Spring Boot 3.x para despliegue WAR en Tomcat externo.

**Cambios requeridos en `pom.xml`:**

```xml
<!-- 1. Cambiar packaging -->
<packaging>war</packaging>

<!-- 2. Excluir Tomcat embebido del WAR (Tomcat externo lo provee) -->
<dependency>
    <groupId>org.springframework.boot</groupId>
    <artifactId>spring-boot-starter-tomcat</artifactId>
    <scope>provided</scope>
</dependency>
```

**Cambios requeridos en `BadInventoryApiApplication.java`:**

```java
import org.springframework.boot.web.servlet.support.SpringBootServletInitializer;
import org.springframework.boot.builder.SpringApplicationBuilder;

@SpringBootApplication
public class BadInventoryApiApplication extends SpringBootServletInitializer {

    @Override
    protected SpringApplicationBuilder configure(SpringApplicationBuilder application) {
        return application.sources(BadInventoryApiApplication.class);
    }

    public static void main(String[] args) {
        SpringApplication.run(BadInventoryApiApplication.class, args);
    }
}
```

**Versión de Tomcat requerida**: Tomcat 10.x (compatible con Jakarta EE 10).
Tomcat 9.x usa Java EE 8 (`javax.*`) y es **incompatible** con Spring Boot 3.x.

**Alternativas consideradas**:
- Mantener packaging JAR → rechazado: bloquea US2 (despliegue en Tomcat externo).
- Usar `war` overlay con Tomcat embebido → rechazado: innecesariamente complejo para 1 microservicio.

---

## Decisión 5 — Ausencia de Suite de Pruebas: SmokeTest Mínimo

**Decisión**: Crear `src/test/java/com/badcode/SmokeTest.java` con 3 pruebas de
integración (una por endpoint) para satisfacer el gate de Paridad Funcional
(Constitución, Principio IV).

**Justificación**: La Constitución establece que "La validación se hará mediante
la suite de pruebas actual" pero no existe ninguna prueba. La Constitución también
permite "pruebas adicionales estrictamente necesarias para cubrir cambios obligatorios
de la migración". Un SmokeTest que valide que los 3 endpoints responden en Java 21
es el mínimo indispensable para cumplir el Principio IV y es la única forma
reproducible y automatizable de evidenciar paridad funcional.

**Alcance del SmokeTest (3 casos):**

| Test | Endpoint | Validación |
|------|----------|-----------|
| `shouldListProducts` | `GET /products` | HTTP 200, body es lista JSON |
| `shouldCreateProduct` | `POST /products` | HTTP 201, body contiene "Producto creado" |
| `shouldGetProductById` | `GET /products/1` | HTTP 200, body es mapa con campo `TITLE` |

**Tecnología**: `@SpringBootTest` + `MockMvc` (incluido en `spring-boot-starter-test`
que Spring Boot añade automáticamente al test scope).

**Alternativas consideradas**:
- No crear tests, validación 100% manual → rechazado: no reproducible, no deja
  evidencia verificable, viola el espíritu del Principio IV.
- Crear tests de lógica de negocio (unitarios del GodController) → rechazado:
  excede el scope mínimo; viola Principio I (no es un cambio obligatorio del stack).

---

## Decisión 6 — `ParameterNameDiscoverer` y APIs Eliminadas en Java 17

**Decisión**: No aplica modificación. `ParameterNameDiscoverer` no está presente
en el código fuente de `bad-inventory-api`.

**Contexto**: Los argumentos del plan mencionan la sustitución de APIs eliminadas
como `ParameterNameDiscoverer`. Spring Framework 6.x (usado por Spring Boot 3.x)
deprecó `DefaultParameterNameDiscoverer` basado en ASM para la detección de nombres
de parámetros sin información de debug. Este cambio afecta proyectos que usen
inyección por nombre de parámetro. `GodController` y `BadInventoryApiApplication`
no usan este mecanismo.

**Otros módulos internos de Java eliminados en Java 17+:**
- `sun.misc.Unsafe` — no presente en el código fuente.
- `com.sun.*` — no presente en el código fuente.
- Confirmado: ninguna referencia a APIs internas de JVM en el proyecto.

---

## Decisión 7 — Compatibilidad de `application.properties`

**Decisión**: `application.properties` permanece sin cambios.

**Propiedades verificadas:**

| Propiedad | Estado en Spring Boot 3.3.x |
|-----------|----------------------------|
| `spring.datasource.url` | ✅ Sin cambios |
| `spring.datasource.driverClassName` | ✅ Sin cambios (org.h2.Driver existe en H2 2.x) |
| `spring.datasource.username` | ✅ Sin cambios |
| `spring.datasource.password` | ✅ Sin cambios |
| `server.port` | ✅ Sin cambios |
| `spring.h2.console.enabled` | ✅ Sin cambios |
| `management.endpoints.web.exposure.include` | ✅ Sin cambios (sin Spring Security, comportamiento idéntico) |

---

## Decisión 8 — Estrategia para 27 Microservicios (Contexto del Plan de Ejecución)

**Decisión**: Este microservicio (`bad-inventory-api`) funciona como **piloto** para
validar el patrón de migración antes de aplicarlo a los 27 microservicios de producción.

**Patrón replicable por microservicio:**

1. `pom.xml`: Actualizar `spring-boot-starter-parent` a 3.3.x + `<java.version>21</java.version>`
2. Búsqueda global de `javax.*` → reemplazar por `jakarta.*` (sed o IDE refactor)
3. `main class`: Extender `SpringBootServletInitializer` si se requiere WAR
4. Verificar compatibilidad de drivers de BD específicos de cada microservicio
5. Ejecutar `mvn clean package` + SmokeTest de los endpoints principales

**Nota sobre Fases del Plan de Ejecución de 27 microservicios (del argumento):**

| Fase del argumento | Correspondencia en este plan | Observación |
|--------------------|------------------------------|-------------|
| Fase 1: GapAnalysis | Este research.md | Análisis de brechas completado |
| Fase 2: Actualización de POM | Tarea T001 (ver tasks.md) | pom.xml cambio de versiones |
| Fase 3: Refactorización javax→jakarta | Tarea T002 | 1 import en este proyecto |
| Fase 4: Suite de pruebas | Tarea T003 + T004 | SmokeTest + ejecución |
| Fase 5: Despliegue Citrix/QA, New Relic/Datadog | Post-migration (fuera del scope de tasks.md) | Requiere infraestructura de QA |

**Telemetría (New Relic/Datadog):** No configurable desde el código de este
microservicio sin agentes externos. La comparativa de telemetría pre/post migración
es responsabilidad del equipo de operaciones en el entorno Citrix/QA.

---

## Regla de Triage de Fallos vs Baseline

Cuando una prueba falle durante o después de la migración, aplicar la siguiente
clasificación antes de crear acciones correctivas:

1. **Tipo A - Regresión de migración**
   - Definición: Cambio en status HTTP, estructura de respuesta, headers, o falla de arranque
     que NO existía en baseline Java 8.
   - Acción: Corregir dentro de este proyecto porque rompe paridad funcional.

2. **Tipo B - Bug preexistente heredado**
   - Definición: Comportamiento incorrecto ya presente en Java 8 (por ejemplo,
     HTTP 200 en errores, SQL injection, manejo pobre de excepciones).
   - Acción: NO corregir en este proyecto; registrar issue separado para backlog.

3. **Tipo C - Diferencia de entorno no funcional**
   - Definición: Variaciones de infraestructura (Tomcat/JVM/telemetría) sin
     impacto observable en contrato de API.
   - Acción: Documentar en checklist QA y observar; no bloquear release si contrato se mantiene.
