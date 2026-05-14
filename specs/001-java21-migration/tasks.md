# Tasks: Migración Tecnológica Java 8 → Java 21

**Input**: Design documents from `/specs/001-java21-migration/`
**Prerequisites**: plan.md (required), spec.md (required), research.md, data-model.md, contracts/

**Tests**: Incluidos porque la especificación exige validación de paridad funcional.
**Organization**: Tareas agrupadas por historia de usuario para implementación y validación independiente.

## Phase 1: Setup (Shared Infrastructure)

**Purpose**: Preparar evidencia base y artefactos de diagnóstico para la migración.

- [X] T001 Capturar baseline Java 8 de respuestas HTTP en specs/001-java21-migration/baseline-api-snapshots.md
- [X] T002 [P] Ejecutar análisis de brechas de dependencias y guardar reporte en specs/001-java21-migration/gap-analysis.md
- [X] T003 [P] Definir checklist de observabilidad QA (Citrix/New Relic/Datadog) en specs/001-java21-migration/qa-observability-checklist.md

---

## Phase 2: Foundational (Blocking Prerequisites)

**Purpose**: Cambios técnicos base que bloquean todas las historias.

**⚠️ CRITICAL**: No iniciar historias de usuario hasta completar esta fase.

- [X] T004 Actualizar parent Spring Boot a 3.3.x y Java a 21 en pom.xml
- [X] T005 Configurar build Maven compatible con Java 21 (compiler/surefire) en pom.xml
- [X] T006 Añadir soporte de pruebas de integración para migración en pom.xml
- [X] T007 Crear esqueleto de pruebas SmokeTest para la migración en src/test/java/com/badcode/SmokeTest.java
- [X] T008 Registrar matriz de comandos de build/run/test para Java 21 en specs/001-java21-migration/quickstart.md

**Checkpoint**: Fundaciones completas; historias de usuario habilitadas.

---

## Phase 3: User Story 1 - Compilación y arranque en el nuevo stack (Priority: P1) 🎯 MVP

**Goal**: Garantizar compilación y arranque correcto en Java 21 / Spring Boot 3.3.x sin alterar lógica de negocio.

**Independent Test**: `mvn clean package -DskipTests` y arranque exitoso de aplicación en Java 21.

### Tests for User Story 1

- [X] T009 [US1] Agregar prueba de arranque de contexto Spring en src/test/java/com/badcode/SmokeTest.java
- [X] T010 [US1] Agregar prueba de contrato para GET /products en src/test/java/com/badcode/SmokeTest.java

### Implementation for User Story 1

- [X] T011 [US1] Migrar import javax.annotation.PostConstruct a jakarta.annotation.PostConstruct en src/main/java/com/badcode/GodController.java
- [X] T012 [US1] Verificar compatibilidad de propiedades runtime en Spring Boot 3.3.x en src/main/resources/application.properties
- [X] T013 [US1] Actualizar guía de validación de arranque Java 21 en specs/001-java21-migration/quickstart.md

**Checkpoint**: US1 funcional y validable de forma independiente.

---

## Phase 4: User Story 2 - Despliegue en Tomcat externo (Priority: P2)

**Goal**: Habilitar despliegue WAR en Tomcat 10.x manteniendo comportamiento funcional.

**Independent Test**: `mvn clean package` genera WAR y despliegue exitoso en Tomcat 10.x externo.

### Tests for User Story 2

- [X] T014 [US2] Agregar prueba de bootstrap servlet para despliegue externo en src/test/java/com/badcode/SmokeTest.java

### Implementation for User Story 2

- [X] T015 [US2] Extender SpringBootServletInitializer y definir configure() en src/main/java/com/badcode/BadInventoryApiApplication.java
- [X] T016 [US2] Configurar packaging WAR y dependencia Tomcat provided en pom.xml
- [X] T017 [US2] Actualizar procedimiento de despliegue Tomcat externo en specs/001-java21-migration/quickstart.md

**Checkpoint**: US2 funcional y validable de forma independiente.

---

## Phase 5: User Story 3 - Paridad funcional validada por suite de pruebas (Priority: P3)

**Goal**: Probar que los endpoints mantienen comportamiento equivalente al baseline Java 8.

**Independent Test**: `mvn test` en Java 21 con pruebas de paridad de endpoints en verde.

### Tests for User Story 3

- [X] T018 [US3] Agregar prueba de paridad para POST /products en src/test/java/com/badcode/SmokeTest.java
- [X] T019 [US3] Agregar prueba de paridad para GET /products/{id} en src/test/java/com/badcode/SmokeTest.java
- [X] T020 [US3] Agregar aserciones de invariantes de contrato (status/body/headers) en src/test/java/com/badcode/SmokeTest.java

### Implementation for User Story 3

- [X] T021 [US3] Registrar evidencia comparativa Java 8 vs Java 21 en specs/001-java21-migration/parity-report.md
- [X] T022 [US3] Documentar regla de triage de fallos vs baseline en specs/001-java21-migration/research.md

**Checkpoint**: US3 funcional y validable de forma independiente.

---

## Phase 6: Polish & Cross-Cutting Concerns

**Purpose**: Cierre transversal y preparación para ejecución repetible en más microservicios.

- [X] T023 [P] Ejecutar validación completa de quickstart y ajustar comandos en specs/001-java21-migration/quickstart.md
- [X] T024 [P] Consolidar patrón replicable para 27 microservicios en specs/001-java21-migration/rollout-pattern.md
- [X] T025 Registrar checklist final de validación Maven/Tomcat en specs/001-java21-migration/final-validation.md

---

## Dependencies & Execution Order

### Phase Dependencies

- **Phase 1 (Setup)**: sin dependencias.
- **Phase 2 (Foundational)**: depende de Phase 1 y bloquea todas las historias.
- **Phase 3 (US1)**: depende de Phase 2.
- **Phase 4 (US2)**: depende de Phase 2; recomendado después de US1 por cambios en pom.xml.
- **Phase 5 (US3)**: depende de Phase 2; recomendado después de US1 para validar paridad sobre build estable.
- **Phase 6 (Polish)**: depende de US1, US2 y US3 completadas.

### User Story Dependencies

- **US1 (P1)**: inicia primero tras Foundational; habilita build estable para el resto.
- **US2 (P2)**: puede avanzar tras Foundational, pero comparte pom.xml con US1.
- **US3 (P3)**: puede avanzar tras Foundational, pero sus pruebas dependen de endpoints ya migrados.

### Within Each User Story

- Pruebas primero.
- Cambios de configuración/infraestructura después.
- Validación independiente al final de cada historia.

### Parallel Opportunities

- `T002` y `T003` pueden correr en paralelo (archivos distintos).
- `T023` y `T024` pueden correr en paralelo (archivos distintos).
- El resto de tareas no se marcan paralelas por colisión en `pom.xml` y `src/test/java/com/badcode/SmokeTest.java`.

---

## Parallel Example: User Story 1

```bash
# No hay tareas [P] en US1 por trabajo concurrente sobre el mismo archivo:
# src/test/java/com/badcode/SmokeTest.java
```

## Parallel Example: User Story 2

```bash
# No hay tareas [P] en US2 por dependencia directa entre bootstrap y packaging.
```

## Parallel Example: User Story 3

```bash
# No hay tareas [P] en US3 por trabajo concurrente sobre el mismo archivo:
# src/test/java/com/badcode/SmokeTest.java
```

---

## Implementation Strategy

### MVP First (User Story 1 Only)

1. Completar Setup (Phase 1).
2. Completar Foundational (Phase 2).
3. Completar US1 (Phase 3).
4. Validar build/arranque en Java 21.
5. Hacer checkpoint de aprobación técnica.

### Incremental Delivery

1. Entregar US1 (compilación + arranque).
2. Entregar US2 (WAR + Tomcat externo).
3. Entregar US3 (paridad automatizada).
4. Cerrar con evidencias y patrón de réplica para 27 microservicios.

### Parallel Team Strategy

1. Equipo A: Setup + Foundational.
2. Equipo B: US1.
3. Equipo C: US2 y US3 de forma secuencial una vez estable US1.

---

## Notes

- Todas las tareas cumplen formato checklist estricto: `- [ ] T### [P?] [US?] Descripción con path`.
- No se incluyen tareas de bug fixing ni refactor estético (alineado a la constitución).
- Las tareas sobre `pom.xml` y `SmokeTest.java` se mantienen secuenciales para evitar conflictos de merge.
