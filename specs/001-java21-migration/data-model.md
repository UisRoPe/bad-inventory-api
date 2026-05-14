# Data Model: Migración Java 8 → Java 21

**Feature**: `001-java21-migration`
**Generated**: 2026-05-14

---

## Entidades del Dominio

### Product

Representa un artículo de inventario almacenado en la base de datos H2 embebida.

| Campo | Tipo Java 8 (H2 1.4.x) | Tipo Java 21 (H2 2.2.x) | Notas |
|-------|------------------------|--------------------------|-------|
| `id` | `INT AUTO_INCREMENT PK` | `INT AUTO_INCREMENT PK` | Sin cambio |
| `title` | `VARCHAR(100)` | `VARCHAR(100)` | Sin cambio |
| `price` | `DOUBLE` | `DOUBLE` (alias de `DOUBLE PRECISION`) | Compatible; H2 2.x acepta `DOUBLE` sin modificación |
| `description` | `VARCHAR(500)` | `VARCHAR(500)` | Sin cambio |
| `category` | `VARCHAR(100)` | `VARCHAR(100)` | Sin cambio |

**DDL actual (compatible con H2 2.x sin modificación):**

```sql
CREATE TABLE IF NOT EXISTS products (
    id          INT AUTO_INCREMENT PRIMARY KEY,
    title       VARCHAR(100),
    price       DOUBLE,
    description VARCHAR(500),
    category    VARCHAR(100)
)
```

**Datos de seed (hardcodeados en `GodController.setupDb()`, sin cambio):**

```sql
INSERT INTO products (title, price, description, category)
VALUES ('Laptop Xiaomi Pro', 1200.0, 'Laptop potente', 'electronics');

INSERT INTO products (title, price, description, category)
VALUES ('Monitor UltraWide', 340.5, 'Monitor de 34 pulgadas', 'electronics');
```

---

## Consideraciones de Migración del Modelo de Datos

### Compatibilidad H2 1.4.x → 2.2.x

H2 2.x introdujo cambios en el modo de compatibilidad SQL. Los siguientes puntos
son relevantes para este proyecto:

- **`DOUBLE`**: Sigue siendo un alias válido en H2 2.x. No requiere cambio.
- **`AUTO_INCREMENT`**: Sintaxis MySQL-compatible soportada en H2 2.x via
  `SET MODE MySQL` o directamente. El modo por defecto acepta `AUTO_INCREMENT`.
- **Resultado de `queryForList`**: `JdbcTemplate.queryForList` devuelve
  `List<Map<String, Object>>`. En H2 2.x, los nombres de columna devueltos en el
  mapa son en **MAYÚSCULAS** (comportamiento consistente con H2 1.4.x en modo por
  defecto). El SmokeTest debe validar con `TITLE`, `PRICE`, etc.

### Estado de Datos entre Java 8 y Java 21

La base de datos es **in-memory** (`jdbc:h2:mem:testdb`). No hay datos persistidos
entre ejecuciones. El esquema y datos de seed se recrean en cada arranque via
`@PostConstruct`. El comportamiento es idéntico antes y después de la migración.

---

## Mapa de Tipos de Datos en Respuestas HTTP

Los endpoints de la API devuelven `Map<String, Object>` serializado como JSON por
Jackson. Los tipos de datos en la respuesta JSON son determinados por H2 y JdbcTemplate:

| Campo DB | Tipo SQL | Tipo Java resultante | JSON |
|----------|----------|---------------------|------|
| `ID` | INT | `Integer` | `number` |
| `TITLE` | VARCHAR | `String` | `string` |
| `PRICE` | DOUBLE | `Double` | `number` |
| `DESCRIPTION` | VARCHAR | `String` | `string` |
| `CATEGORY` | VARCHAR | `String` | `string` |

**Nota**: Jackson (versión gestionada por Spring Boot 3.x BOM) serializa `Double`
como número decimal. El comportamiento es idéntico al de Jackson en Spring Boot 2.x
para estos tipos primitivos.
