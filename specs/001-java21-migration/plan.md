# Implementation Plan: Migración Tecnológica Java 8 → Java 21

**Branch**: `001-java21-migration` | **Date**: 2026-05-14 | **Spec**: [spec.md](spec.md)
**Input**: Feature specification from `specs/001-java21-migration/spec.md`

## Summary

Migrar `bad-inventory-api` de Java 1.8 / Spring Boot 2.7.18 a Java 21 (LTS) /
Spring Boot 3.3.x garantizando paridad funcional absoluta en los 3 endpoints REST
existentes (`GET /products`, `POST /products`, `GET /products/{id}`).

**Archivos de código fuente afectados (total: 3):**
- `pom.xml` — versión Java, parent Spring Boot, packaging WAR, dependencia `tomcat:provided`
- `src/main/java/com/badcode/BadInventoryApiApplication.java` — extender `SpringBootServletInitializer`
- `src/main/java/com/badcode/GodController.java` — reemplazar `javax.annotation.PostConstruct` → `jakarta.annotation.PostConstruct`

**Hallazgo crítico:** El repositorio **no contiene suite de pruebas**. La validación
de paridad funcional se realizará mediante un test de humo mínimo (3 pruebas de
integración), justificado como obligatorio por la Constitución, Principio IV.

## Technical Context

**Language/Version**: Java 1.8 (origen) → Java 21 LTS (destino)
**Primary Dependencies**: spring-boot-starter-parent 2.7.18 → 3.3.x; spring-boot-starter-web; spring-boot-starter-jdbc; H2 2.x (gestionado por BOM 3.x)
**Storage**: H2 in-memory (`jdbc:h2:mem:testdb`); esquema creado en runtime por `@PostConstruct` en `GodController`
**Testing**: No existe suite. Se creará un `SmokeTest` de integración (3 casos) para validar paridad
**Target Platform**: JVM 21 (modo embebido) + Tomcat 10.x externo (WAR deployment)
**Project Type**: web-service (REST API — Spring MVC)
**Performance Goals**: Paridad con Java 8; sin nuevos SLAs definidos
**Constraints**: Cero regresiones funcionales; cambios limitados a lo exigido por Breaking Changes
**Scale/Scope**: 1 microservicio, 3 endpoints HTTP, 1 tabla relacional (products)

## Constitution Check

*GATE: Must pass before Phase 0 research. Re-check after Phase 1 design.*

| Principio | Estado | Detalle |
|-----------|--------|---------|
| I. Migración Técnica Únicamente | ✅ PASS | Solo se modifican pom.xml, un import javax→jakarta, y se añade SpringBootServletInitializer. Ningún cambio de lógica de negocio. |
| II. No Bug Fixing | ✅ PASS | Los bugs existentes (SQL injection, swallow exceptions, HTTP 200 en errores) están documentados en research.md pero **no se corrigen**. |
| III. No Code Smells | ✅ PASS | No se renombran variables, no se extrae lógica, no se reorganizan paquetes. GodController permanece intacto salvo el import. |
| IV. Paridad Funcional | ⚠️ PASS CON JUSTIFICACIÓN | No existe suite de pruebas. Se crea un `SmokeTest` mínimo (3 pruebas de integración) estrictamente para cubrir el gate de paridad exigido por la Constitución. Esta creación es el "cambio obligatorio de la migración" (ver Complexity Tracking). |
| V. Aislamiento de Cambios de Performance | ✅ PASS | No se activan Virtual Threads ni APIs nuevas de Java 21. El BOM de Spring Boot 3.x puede activar mejoras internas, pero no hay cambio intencional de performance. |

**Resultado del gate pre-investigación:** ✅ APROBADO (1 justificación documentada)

*Re-evaluación post-diseño (Fase 1):* Ver sección al final del documento.

## Project Structure

### Documentation (this feature)

```text
specs/001-java21-migration/
├── plan.md              # Este archivo
├── research.md          # Fase 0: análisis de brechas y decisiones técnicas
├── data-model.md        # Fase 1: entidades y esquema de datos
├── quickstart.md        # Fase 1: guía para ejecutar el proyecto migrado
├── contracts/           # Fase 1: contrato de la API REST
│   └── api-contract.md
└── tasks.md             # Fase 2: generado por /speckit.tasks (NO creado aquí)
```

### Source Code (repository root)

```text
pom.xml                          # [MODIFICAR] Java 21, Spring Boot 3.3.x, packaging WAR

src/
├── main/
│   ├── java/
│   │   └── com/badcode/
│   │       ├── BadInventoryApiApplication.java  # [MODIFICAR] + SpringBootServletInitializer
│   │       └── GodController.java               # [MODIFICAR] javax → jakarta import
│   └── resources/
│       └── application.properties               # [VERIFICAR] compatibilidad SBoot 3.x
└── test/
    └── java/
        └── com/badcode/
            └── SmokeTest.java                   # [CREAR] 3 tests de integración (gate paridad)
```

**Structure Decision**: Single project — estructura Maven estándar. La migración
no introduce nuevos módulos ni cambia la topología de paquetes.

## Complexity Tracking

| Violación | Por qué es necesaria | Alternativa más simple rechazada |
|-----------|---------------------|----------------------------------|
| Crear `SmokeTest.java` (prueba nueva) | Sin ninguna prueba, el gate de Paridad Funcional (Principio IV) es invalorable. La Constitución admite explícitamente "pruebas adicionales estrictamente necesarias para cubrir cambios obligatorios de la migración". | Manual testing only — rechazado: no reproducible, no automatizable, no deja evidencia. |
| Cambiar packaging de JAR a WAR | La spec requiere despliegue en Tomcat externo (FR-004, US2). Spring Boot 3.x soporta packaging WAR con `SpringBootServletInitializer`. | Permanece JAR — rechazado: no cumple FR-004, bloquea US2 completamente. |

---

## Constitution Check — Re-evaluación Post-Diseño (Fase 1)

*Re-ejecutada tras completar research.md, data-model.md, contracts/ y quickstart.md.*

| Principio | Estado Post-Diseño | Cambio vs Pre-Research |
|-----------|-------------------|----------------------|
| I. Migración Técnica Únicamente | ✅ CONFIRMADO | El análisis reveló exactamente 3 archivos a modificar. Sin nuevas funcionalidades. |
| II. No Bug Fixing | ✅ CONFIRMADO | SQL Injection, swallow exceptions y HTTP 200 en errores documentados en api-contract.md como comportamiento heredado intocable. |
| III. No Code Smells | ✅ CONFIRMADO | data-model.md y contracts/ describen el estado actual sin proponer refactorizaciones. |
| IV. Paridad Funcional | ✅ JUSTIFICACIÓN MANTENIDA | Los contratos en contracts/api-contract.md establecen los invariantes de paridad. El SmokeTest valida exactamente esos invariantes. |
| V. Aislamiento de Performance | ✅ CONFIRMADO | H2 2.x gestionado por BOM; sin activación intencional de Virtual Threads. |

**Resultado del gate post-diseño:** ✅ APROBADO — Plan listo para `/speckit.tasks`
