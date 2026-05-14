# Parity Report - Java 8 vs Java 21

Fecha: 2026-05-14
Feature: 001-java21-migration

## Fuentes comparadas

- Baseline contractual: baseline-api-snapshots.md
- Contrato funcional: contracts/api-contract.md
- Evidencia post-migración: SmokeTest.java + ejecución de Maven (mvn test)

## Matriz de paridad

| Endpoint | Baseline Java 8 | Post-migración Java 21 | Estado |
|----------|------------------|------------------------|--------|
| GET /products | 200 + lista JSON | Test shouldListProducts + shouldKeepCorsAndStatusInvariants | PASS |
| POST /products | 201 en éxito + texto "Producto creado." | Test shouldCreateProductWithLegacyResponseContract | PASS |
| GET /products/{id} | 200 + objeto JSON | Test shouldGetProductByIdWithLegacyResponseContract | PASS |
| CORS | Access-Control-Allow-Origin: * | Test shouldKeepCorsAndStatusInvariants | PASS |

## Notas

- Se mantiene intencionalmente el comportamiento heredado (incluyendo anti-patrones)
  para cumplir la constitución de migración as-is.
- Cualquier fallo futuro debe clasificarse con la regla de triage documentada en research.md.
