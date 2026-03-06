package com.badcode;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.client.RestTemplate;

import javax.annotation.PostConstruct;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;

// MALA PRACTICA 1: God Class. Todo en un solo archivo (Controlador, Lógica de Negocio, Acceso a Datos)
// MALA PRACTICA 2: CORS abierto a todo el mundo
@RestController
@RequestMapping("/products")
@CrossOrigin(origins = "*")
public class GodController {

    // MALA PRACTICA 3: Inyección de dependencias directamente en el campo sin
    // constructor
    // MALA PRACTICA 4: Malos nombres de variables (t en lugar de jdbcTemplate)
    @Autowired
    private JdbcTemplate t;

    // MALA PRACTICA 5: Uso de estado compartido mutable sin sincronización
    // (Thread-Safety issue y Memory Leak)
    public static List<Map<String, Object>> cachingSystem = new ArrayList<>();

    @PostConstruct
    public void setupDb() {
        try {
            // MALA PRACTICA 6: Creación de tablas e inserción de datos "hardcodeada" en el
            // controlador
            t.execute(
                    "CREATE TABLE IF NOT EXISTS products (id INT AUTO_INCREMENT PRIMARY KEY, title VARCHAR(100), price DOUBLE, description VARCHAR(500), category VARCHAR(100))");
            t.execute(
                    "INSERT INTO products (title, price, description, category) VALUES ('Laptop Xiaomi Pro', 1200.0, 'Laptop potente', 'electronics')");
            t.execute(
                    "INSERT INTO products (title, price, description, category) VALUES ('Monitor UltraWide', 340.5, 'Monitor de 34 pulgadas', 'electronics')");
        } catch (Exception e) {
            // MALA PRACTICA 7: Tragarse la excepción sin hacer nada (Swallow Exception)
        }
    }

    // GET /products
    @GetMapping
    public Object listAll() { // MALA PRACTICA 8: Retornar tipos genéricos Object o colecciones sin tipar bien
                              // en la firma
        try {
            if (!cachingSystem.isEmpty()) {
                return cachingSystem;
            }

            // MALA PRACTICA 9: Uso de Magic Strings (URL quemada en código) y peticiones
            // sincrónicas lentas que pueden fallar
            RestTemplate rest = new RestTemplate();
            try {
                String reqVar = rest.getForObject("https://fakestoreapi.com/products", String.class);
                System.out.println("LOG INFO POBRE: " + reqVar); // MALA PRACTICA 10: Usar System.out en vez de un
                                                                 // Logger
            } catch (Exception ex) {
                // ignorado deliberadamente
            }

            List<Map<String, Object>> result = t.queryForList("SELECT * FROM products");
            cachingSystem = result; // No es thread-safe

            return result;
        } catch (Exception ex) {
            // MALA PRACTICA 11: Retornar 200 OK siempre, escondiendo el error al cliente
            return new ResponseEntity<>("{\"error\": \"Algo salio mal pero te devolvemos 200\"}", HttpStatus.OK);
        }
    }

    // POST /products
    @PostMapping
    public ResponseEntity<?> createProduct(@RequestBody Map<String, Object> req) { // MALA PRACTICA 12: Usar Map
                                                                                   // genérico en vez de un DTO validado
        try {
            // MALA PRACTICA 13: Variables temporales inútiles y nombres no descriptivos
            String var1 = req.get("title") != null ? req.get("title").toString() : "";
            String var2 = req.get("price") != null ? req.get("price").toString() : "0.0";
            String var3 = req.get("description") != null ? req.get("description").toString() : "";

            // MALA PRACTICA 14: PELIGROSISIMA. Inyección SQL clásica concatenando strings
            // del usuario directamente en la query
            String query = "INSERT INTO products (title, price, description, category) VALUES ('"
                    + var1 + "', "
                    + var2 + ", '"
                    + var3 + "', 'unknown')";

            t.execute(query);

            // "Invalida" caché
            cachingSystem.clear();

            return new ResponseEntity<>("Producto creado.", HttpStatus.CREATED);
        } catch (Exception oops) {
            oops.printStackTrace();
            // Retorna 200 OK aunque el query fallara
            return new ResponseEntity<>("Todo bien (mentira, hubo error)", HttpStatus.OK);
        }
    }

    // GET /products/{id}
    @GetMapping("/{id}")
    public ResponseEntity<?> getProductById(@PathVariable("id") String idX) { // MALA PRACTICA 15: Recibir String cuando
                                                                              // debería ser Int para la DB
        try {
            // MALA PRACTICA 14 (Repetida): Inyección SQL en el WHERE
            String s = "SELECT * FROM products WHERE id = " + idX;
            List<Map<String, Object>> rows = t.queryForList(s);

            if (rows.isEmpty()) {
                // MALA PRACTICA 16: Formato de respuesta JSON hardcodeado o inconsistente
                return new ResponseEntity<>("{ \"mensaje\": \"No se encontró el producto\" }", HttpStatus.OK); // Más
                                                                                                               // 200 OK
                                                                                                               // falsos
            }

            return new ResponseEntity<>(rows.get(0), HttpStatus.OK);
        } catch (Exception genericException) {
            System.err.println("Fatal error: " + genericException.getMessage());
            return new ResponseEntity<>("Excepcion controlada en getById", HttpStatus.OK);
        }
    }
}
