# API Contract: bad-inventory-api

**Feature**: `001-java21-migration`
**Base URL**: `http://{host}:{port}`
**Server Port (default)**: `8080`
**Contexto WAR (Tomcat externo)**: `http://{host}:{port}/bad-inventory-api`

> **Nota de migración**: El contrato de la API es **idéntico** antes y después de la
> migración. Cualquier diferencia en respuestas constituye una regresión y debe
> reportarse como hallazgo de migración.

---

## Endpoints

### 1. Listar todos los productos

**`GET /products`**

**Descripción**: Retorna todos los productos del inventario. Intenta obtener datos
de FakeStore API; si falla, retorna desde la base de datos local H2.

**Request**: Sin parámetros, sin body.

**Response 200 OK** (caso normal — base de datos local):

```json
[
  {
    "ID": 1,
    "TITLE": "Laptop Xiaomi Pro",
    "PRICE": 1200.0,
    "DESCRIPTION": "Laptop potente",
    "CATEGORY": "electronics"
  },
  {
    "ID": 2,
    "TITLE": "Monitor UltraWide",
    "PRICE": 340.5,
    "DESCRIPTION": "Monitor de 34 pulgadas",
    "CATEGORY": "electronics"
  }
]
```

**Response 200 OK** (caso error — comportamiento heredado de la versión Java 8):

```json
{"error": "Algo salio mal pero te devolvemos 200"}
```

> **Nota**: Este endpoint devuelve HTTP 200 incluso en caso de error. Este es un
> bug conocido de la versión Java 8 que **NO se corrige** (Principio II de la Constitución).

---

### 2. Crear un producto

**`POST /products`**

**Descripción**: Inserta un nuevo producto en la base de datos H2.

**Request Body** (application/json):

```json
{
  "title": "string",
  "price": "string (número como string)",
  "description": "string"
}
```

> **Nota**: El campo `category` se asigna automáticamente como `'unknown'` en el INSERT SQL.
> El campo `price` se recibe como String aunque represente un número (comportamiento del Java 8 original).

**Response 201 Created** (caso normal):

```
Producto creado.
```

**Response 200 OK** (caso error — comportamiento heredado):

```
Todo bien (mentira, hubo error)
```

> **Nota**: El endpoint tiene una vulnerabilidad de SQL Injection en la versión Java 8
> (concatenación directa de strings en el query). **No se corrige** (Principio II).

---

### 3. Obtener producto por ID

**`GET /products/{id}`**

**Descripción**: Retorna un único producto por su ID numérico.

**Path Parameter**:

| Parámetro | Tipo | Descripción |
|-----------|------|-------------|
| `id` | `String` (acepta entero) | ID del producto en la base de datos |

**Response 200 OK** (producto encontrado):

```json
{
  "ID": 1,
  "TITLE": "Laptop Xiaomi Pro",
  "PRICE": 1200.0,
  "DESCRIPTION": "Laptop potente",
  "CATEGORY": "electronics"
}
```

**Response 200 OK** (producto no encontrado — comportamiento heredado):

```json
{ "mensaje": "No se encontró el producto" }
```

> **Nota**: El endpoint devuelve HTTP 200 para producto no encontrado (en lugar de 404).
> Bug conocido de Java 8 — **no se corrige** (Principio II).
> El parámetro `{id}` se recibe como `String` aunque sea un entero — **no se corrige**
> (Principio I: no es un Breaking Change de Java 21).

---

## Cabeceras Comunes

| Cabecera | Valor | Notas |
|----------|-------|-------|
| `Content-Type` | `application/json` (responses) | Gestionado por Jackson en Spring MVC |
| `Access-Control-Allow-Origin` | `*` | CORS abierto por `@CrossOrigin(origins = "*")` — sin cambio |

---

## Invariantes de Paridad Post-Migración

Los siguientes comportamientos DEBEN ser idénticos en Java 21 para que la migración
sea exitosa:

1. `GET /products` → devuelve lista JSON con claves en MAYÚSCULAS (`ID`, `TITLE`, `PRICE`, `DESCRIPTION`, `CATEGORY`)
2. `POST /products` con body válido → devuelve HTTP 201 con body `"Producto creado."`
3. `GET /products/1` → devuelve HTTP 200 con objeto JSON del primer producto
4. Todos los endpoints mantienen `Access-Control-Allow-Origin: *` en la respuesta
