package com.badcode;

import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.web.servlet.support.SpringBootServletInitializer;
import org.springframework.test.web.servlet.MockMvc;

import static org.assertj.core.api.Assertions.assertThat;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.content;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.header;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@SpringBootTest
@AutoConfigureMockMvc
class SmokeTest {

    @Autowired
    private MockMvc mockMvc;

    @Test
    void contextLoads() {
        assertThat(mockMvc).isNotNull();
    }

    @Test
    void shouldListProducts() throws Exception {
        mockMvc.perform(get("/products"))
                .andExpect(status().isOk());
    }

    @Test
    void shouldSupportExternalTomcatBootstrap() {
        assertThat(SpringBootServletInitializer.class.isAssignableFrom(BadInventoryApiApplication.class)).isTrue();
    }

    @Test
    void shouldCreateProductWithLegacyResponseContract() throws Exception {
        String payload = "{\"title\":\"Test\",\"price\":\"99.9\",\"description\":\"Test product\"}";

        mockMvc.perform(post("/products")
                        .contentType("application/json")
                        .content(payload))
                .andExpect(status().isCreated())
                .andExpect(content().string(org.hamcrest.Matchers.containsString("Producto creado.")));
    }

    @Test
    void shouldGetProductByIdWithLegacyResponseContract() throws Exception {
        mockMvc.perform(get("/products/1"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.TITLE").exists());
    }

    @Test
    void shouldKeepCorsAndStatusInvariants() throws Exception {
        mockMvc.perform(get("/products").header("Origin", "http://example.com"))
                .andExpect(status().isOk())
                .andExpect(header().string("Access-Control-Allow-Origin", "*"));

        mockMvc.perform(post("/products")
                        .header("Origin", "http://example.com")
                        .contentType("application/json")
                        .content("{\"title\":\"A\",\"price\":\"1\",\"description\":\"B\"}"))
                .andExpect(status().isCreated())
                .andExpect(header().string("Access-Control-Allow-Origin", "*"));

        mockMvc.perform(get("/products/1").header("Origin", "http://example.com"))
                .andExpect(status().isOk())
                .andExpect(header().string("Access-Control-Allow-Origin", "*"));
    }
}
