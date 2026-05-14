# Rollout Pattern - Lote de 27 Microservicios

Fecha: 2026-05-14
Baseline pattern origin: bad-inventory-api (001-java21-migration)

## Objetivo

Aplicar un patrón repetible de migración as-is (Java 8/SB2.x -> Java 21/SB3.3.x)
sin cambios de lógica de negocio.

## Secuencia estándar por microservicio

1. Preparación
- Crear rama `NNN-<service>-java21-migration`
- Capturar baseline contractual de API y smoke endpoints
- Ejecutar gap analysis de dependencias

2. Fundaciones
- Actualizar `pom.xml` a Java 21 + Spring Boot 3.3.x
- Migrar imports `javax.*` a `jakarta.*` donde aplique
- Configurar WAR + `SpringBootServletInitializer` si despliega en Tomcat externo

3. Validación
- Ejecutar smoke tests de endpoints críticos
- Validar `mvn test` y `mvn clean package -DskipTests`
- Registrar parity-report por servicio

4. QA/Observabilidad
- Desplegar en Citrix/QA
- Comparar telemetría New Relic/Datadog contra baseline
- Clasificar desviaciones con regla de triage (Tipo A/B/C)

5. Cierre
- Completar final-validation checklist
- Aprobar y pasar al siguiente microservicio

## Criterios de salida por microservicio

- Build en Java 21 exitoso
- Contrato de API sin regresión
- Smoke tests en verde
- Checklist QA/Observabilidad completado
- Evidencia documental almacenada en `specs/<feature>/`

## Plantilla mínima de archivos por microservicio

- baseline-api-snapshots.md
- gap-analysis.md
- parity-report.md
- qa-observability-checklist.md
- final-validation.md
