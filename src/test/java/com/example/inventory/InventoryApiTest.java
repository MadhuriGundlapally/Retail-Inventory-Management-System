package com.example.inventory;

import static org.hamcrest.Matchers.*;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.jayway.jsonpath.JsonPath;
import java.util.Map;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.MvcResult;
import org.springframework.transaction.annotation.Transactional;

@SpringBootTest
@AutoConfigureMockMvc
@Transactional
class InventoryApiTest {

    @Autowired MockMvc mvc;
    @Autowired ObjectMapper json;

    private long createProduct(String sku, int qty, int reorder) throws Exception {
        Map<String, Object> body = Map.of("sku", sku, "name", "Widget " + sku, "category", "Tools",
                "price", 9.99, "initialQuantity", qty, "reorderLevel", reorder);
        MvcResult res = mvc.perform(post("/api/products").contentType(MediaType.APPLICATION_JSON)
                .content(json.writeValueAsString(body))).andExpect(status().isCreated()).andReturn();
        return ((Number) JsonPath.read(res.getResponse().getContentAsString(), "$.id")).longValue();
    }

    @Test
    void createAndFetchProduct() throws Exception {
        long id = createProduct("SKU-1", 10, 2);
        mvc.perform(get("/api/products/" + id)).andExpect(status().isOk())
                .andExpect(jsonPath("$.sku").value("SKU-1"))
                .andExpect(jsonPath("$.quantity").value(10));
    }

    @Test
    void duplicateSkuReturns409() throws Exception {
        createProduct("DUP", 1, 0);
        Map<String, Object> body = Map.of("sku", "DUP", "name", "Other", "price", 1.0);
        mvc.perform(post("/api/products").contentType(MediaType.APPLICATION_JSON)
                .content(json.writeValueAsString(body))).andExpect(status().isConflict());
    }

    @Test
    void invalidProductReturns400WithDetails() throws Exception {
        Map<String, Object> body = Map.of("sku", "", "name", "", "price", -5);
        mvc.perform(post("/api/products").contentType(MediaType.APPLICATION_JSON)
                        .content(json.writeValueAsString(body)))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.details", hasSize(greaterThanOrEqualTo(3))));
    }

    @Test
    void malformedJsonReturns400() throws Exception {
        mvc.perform(post("/api/products").contentType(MediaType.APPLICATION_JSON).content("{bad"))
                .andExpect(status().isBadRequest());
    }

    @Test
    void unknownProductReturns404() throws Exception {
        mvc.perform(get("/api/products/999999")).andExpect(status().isNotFound());
    }

    @Test
    void updateAndDeleteProduct() throws Exception {
        long id = createProduct("UPD", 5, 1);
        Map<String, Object> body = Map.of("sku", "UPD", "name", "Renamed", "price", 19.5, "reorderLevel", 3);
        mvc.perform(put("/api/products/" + id).contentType(MediaType.APPLICATION_JSON)
                        .content(json.writeValueAsString(body)))
                .andExpect(status().isOk()).andExpect(jsonPath("$.name").value("Renamed"))
                .andExpect(jsonPath("$.reorderLevel").value(3));
        mvc.perform(delete("/api/products/" + id)).andExpect(status().isNoContent());
        mvc.perform(get("/api/products/" + id)).andExpect(status().isNotFound());
    }

    @Test
    void listFiltersByCategory() throws Exception {
        createProduct("L-1", 1, 0);
        mvc.perform(get("/api/products").param("category", "tools"))
                .andExpect(status().isOk()).andExpect(jsonPath("$[*].sku", hasItem("L-1")));
        mvc.perform(get("/api/products").param("category", "nothing"))
                .andExpect(jsonPath("$", hasSize(0)));
    }

    @Test
    void adjustStockUpAndDown() throws Exception {
        long id = createProduct("ST-1", 10, 2);
        mvc.perform(post("/api/stock/" + id + "/adjust").contentType(MediaType.APPLICATION_JSON)
                        .content("{\"delta\": 5, \"reason\": \"restock\"}"))
                .andExpect(status().isOk()).andExpect(jsonPath("$.quantity").value(15));
        mvc.perform(post("/api/stock/" + id + "/adjust").contentType(MediaType.APPLICATION_JSON)
                        .content("{\"delta\": -3}"))
                .andExpect(jsonPath("$.quantity").value(12));
    }

    @Test
    void removingMoreThanAvailableReturns409() throws Exception {
        long id = createProduct("ST-2", 2, 0);
        mvc.perform(post("/api/stock/" + id + "/adjust").contentType(MediaType.APPLICATION_JSON)
                        .content("{\"delta\": -3}"))
                .andExpect(status().isConflict());
        mvc.perform(get("/api/stock/" + id)).andExpect(jsonPath("$.quantity").value(2));
    }

    @Test
    void adjustRequiresDelta() throws Exception {
        long id = createProduct("ST-3", 2, 0);
        mvc.perform(post("/api/stock/" + id + "/adjust").contentType(MediaType.APPLICATION_JSON).content("{}"))
                .andExpect(status().isBadRequest());
    }

    @Test
    void lowStockEndpointListsProductsAtOrBelowReorderLevel() throws Exception {
        createProduct("LOW-1", 1, 5);
        createProduct("OK-1", 50, 5);
        mvc.perform(get("/api/stock/low")).andExpect(status().isOk())
                .andExpect(jsonPath("$[*].sku", hasItem("LOW-1")))
                .andExpect(jsonPath("$[*].sku", not(hasItem("OK-1"))));
    }
}
