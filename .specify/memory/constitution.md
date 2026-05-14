<!--
SYNC IMPACT REPORT
==================
Version change:    N/A (plantilla con placeholders) → 1.0.0
Bump rationale:    Constitución inicial — todos los principios y secciones son nuevos.

Principios añadidos:
  I.   Migración Técnica Únicamente       (nuevo)
  II.  No Bug Fixing                      (nuevo)
  III. No Code Smells / Mejoras Estéticas (nuevo)
  IV.  Paridad Funcional                  (nuevo)
  V.   Aislamiento de Cambios de Performance (nuevo)

Secciones añadidas:
  - Alcance Técnico de la Migración
  - Proceso de Validación y Entrega
  - Governance

Templates revisados:
  - .specify/templates/plan-template.md  ✅ (Constitution Check es genérico por diseño; se rellena en /speckit.plan)
  - .specify/templates/spec-template.md  ✅ (sin cambios estructurales requeridos)
  - .specify/templates/tasks-template.md ✅ (sin cambios estructurales requeridos)

TODOs diferidos: ninguno.
-->

# Migración Tecnológica Java 8 → 21 Constitution

## Core Principles

### I. Migración Técnica Únicamente

El alcance de cada cambio DEBE limitarse exclusivamente a lo que sea obligatorio para
compilar y ejecutar el software en Java 21 / Spring Boot 3.x.
No se permitirá agregar funcionalidades, mejorar arquitectura ni realizar ninguna
modificación que no sea requerida por Breaking Changes del nuevo stack.
Cualquier cambio que supere este criterio DEBE rechazarse en revisión de código.

### II. No Bug Fixing

Los defectos presentes en la versión Java 8 en producción NO DEBEN ser corregidos
durante el proceso de migración.
Corregir bugs simultáneamente a la migración introduce ambigüedad en el diagnóstico
de problemas post-migración y dificulta la trazabilidad de incidentes.
Si se detecta un bug durante la migración, DEBE registrarse como issue separado para
atenderse en un sprint posterior.

### III. No Code Smells / Mejoras Estéticas

No se dedicará tiempo a corregir deuda técnica estética, code smells ni
refactorizaciones que no sean exigidas directamente por Breaking Changes de
Java 17/21 o Spring Boot 3.x.
Si una mejora no es requerida por el compilador o el runtime del nuevo stack,
DEBE descartarse de este proyecto de migración.
Esto incluye: renombrado de variables, extracción de métodos, reorganización de
paquetes y cualquier cambio motivado por legibilidad o estética.

### IV. Paridad Funcional

El comportamiento observable del software DEBE ser idéntico al de la versión Java 8
al completar la migración.
La validación DEBE realizarse mediante la suite de pruebas existente.
No se añadirán nuevas pruebas de negocio; las únicas pruebas adicionales admitidas
son las estrictamente necesarias para cubrir cambios obligatorios de la migración
(p.ej., cambios en contratos de serialización provocados por Jakarta).

### V. Aislamiento de Cambios de Performance

Mejoras de rendimiento como Virtual Threads, Records, Sealed Classes u otras nuevas
APIs de Java 21 SOLO DEBEN aplicarse cuando sean necesarias para compensar cambios
estructurales impuestos por el nuevo stack (p.ej., reemplazo de APIs deprecadas sin
equivalente directo).
Está prohibida la optimización proactiva de lógica de negocio bajo el pretexto de la
migración.

## Alcance Técnico de la Migración

Las siguientes categorías de cambios están **autorizadas** en este proyecto:

- **Actualización de dependencias**: `pom.xml` / `build.gradle` a versiones
  compatibles con Java 21 / Spring Boot 3.3.x.
- **Migración `javax.*` → `jakarta.*`**: Obligatoria para todos los artefactos
  afectados por Jakarta EE 10.
- **Eliminación de APIs removidas**: Sustitución de APIs eliminadas entre
  Java 9–21 por sus equivalentes actuales.
- **Compatibilidad con Tomcat externo**: Mantenimiento o adición de
  `SpringBootServletInitializer` para despliegue WAR si aplica.
- **Configuración de runtime**: Cambios en `application.properties` /
  `application.yml` requeridos por Spring Boot 3.x.
- **Records como DTOs inmutables**: Permitido únicamente si la arquitectura del
  nuevo stack lo requiere para la serialización (Jackson, OpenAPI).

Cualquier cambio fuera de esta lista DEBE justificarse explícitamente en la tarea
correspondiente y recibir aprobación del arquitecto responsable antes de
implementarse.

## Proceso de Validación y Entrega

- **Gate de entrada**: Revisión de la lista de Breaking Changes oficial de
  Spring Boot 3.x y Java 21 DEBE completarse antes de iniciar cualquier
  microservicio.
- **Gate de salida**: La suite de pruebas existente DEBE ejecutarse sin
  degradación (mismo número de pruebas en verde que en la versión Java 8).
- **Reporte de cambios por PR**: Cada Pull Request de migración DEBE incluir
  una tabla con: componente afectado, cambio realizado y justificación de
  compatibilidad con los principios de esta constitución.
- **Prohibición de cambios mixtos**: Un PR de migración NO DEBE mezclar cambios
  técnicos obligatorios con mejoras opcionales o correcciones de bugs.
- **Revisión de constitución en PR**: Todo revisor DEBE verificar el cumplimiento
  de los cinco principios como parte del checklist de revisión.

## Governance

Esta constitución es el documento normativo supremo del proyecto de migración.
Toda decisión técnica DEBE evaluarse contra los cinco principios antes de
implementarse.

- Cualquier enmienda DEBE documentarse con: motivo de cambio, impacto en los
  principios existentes y aprobación del arquitecto responsable.
- La versión DEBE incrementarse según Semantic Versioning:
  MAJOR para cambios de principios incompatibles o eliminación de principios;
  MINOR para nuevas secciones o principios añadidos;
  PATCH para clarificaciones, correcciones ortográficas o refinamientos no semánticos.
- Las fechas DEBEN estar en formato ISO 8601 (YYYY-MM-DD).
- El cumplimiento DEBE verificarse en cada revisión de PR como primer paso del
  checklist de revisión de código.

**Version**: 1.0.0 | **Ratified**: 2026-05-14 | **Last Amended**: 2026-05-14
