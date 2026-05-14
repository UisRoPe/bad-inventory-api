# Baseline API Snapshots (Java 8 / Spring Boot 2.7.18)

Fecha de captura: 2026-05-14
Feature: 001-java21-migration

## Método de baseline

No se dispone en este workspace de ejecución simultánea garantizada con JDK 8 para
captura runtime directa. Se toma como baseline contractual la salida esperada
según el código fuente actual y el contrato definido en contracts/api-contract.md.

## Snapshot 1: GET /products

Status esperado: 200
Body esperado (forma): lista JSON con claves en mayúsculas

Ejemplo:

```json
[
  {
    "ID": 1,
    "TITLE": "Laptop Xiaomi Pro",
    "PRICE": 1200.0,
    "DESCRIPTION": "Laptop potente",
    "CATEGORY": "electronics"
  }
]
```

## Snapshot 2: POST /products

Status esperado (caso éxito): 201
Body esperado: "Producto creado."

Status esperado (caso error heredado): 200
Body esperado: "Todo bien (mentira, hubo error)"

## Snapshot 3: GET /products/{id}

Status esperado (id existente): 200
Body esperado (forma): objeto JSON con claves en mayúsculas

Status esperado (id no existente): 200
Body esperado:

```json
{ "mensaje": "No se encontró el producto" }
```

## Criterio de paridad

Cualquier desviación de status HTTP, forma de payload o cabecera CORS respecto de
estos snapshots se considera regresión de migración.
