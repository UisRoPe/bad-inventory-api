# QA Observability Checklist (Citrix / New Relic / Datadog)

Fecha: 2026-05-14
Feature: 001-java21-migration

## Pre-despliegue

- [ ] Confirmar build Java 21 exitoso en CI/CD
- [ ] Confirmar artefacto WAR generado
- [ ] Confirmar entorno QA usa Tomcat 10.x
- [ ] Confirmar variables de entorno y JVM flags equivalentes a baseline

## Despliegue QA/Citrix

- [ ] Aplicación inicia sin ClassNotFoundException de jakarta.*
- [ ] Endpoint GET /products responde 200
- [ ] Endpoint POST /products mantiene semántica heredada (201 éxito / 200 error)
- [ ] Endpoint GET /products/{id} mantiene semántica heredada (200 encontrado/no encontrado)

## Telemetría New Relic

- [ ] Throughput de requests dentro de rango baseline (+/-10%)
- [ ] Error rate sin incremento sostenido post-migración
- [ ] p95 de latencia sin degradación crítica
- [ ] Heap usage y GC dentro de umbrales históricos

## Telemetría Datadog

- [ ] CPU usage dentro de rango baseline (+/-10%)
- [ ] Memory RSS sin crecimiento anómalo
- [ ] Tasa de reinicios de instancia = 0 en ventana de smoke test
- [ ] Logs sin nuevos errores críticos de framework

## Cierre de validación

- [ ] Baseline vs post-migración documentado en parity-report.md
- [ ] Desviaciones registradas y clasificadas (migración vs bug preexistente)
- [ ] Aprobación de QA para paso a siguiente microservicio del lote de 27
