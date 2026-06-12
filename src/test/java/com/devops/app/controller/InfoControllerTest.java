package com.devops.app.controller;

import com.devops.app.config.TestSecurityConfig;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.context.annotation.Import;
import org.springframework.test.web.servlet.MockMvc;

import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@WebMvcTest(InfoController.class)
@Import(TestSecurityConfig.class)
class InfoControllerTest {

    @Autowired MockMvc mockMvc;

    @Test
    void infoEndpointReturnsAppDetails() throws Exception {
        mockMvc.perform(get("/api/"))
            .andExpect(status().isOk())
            .andExpect(jsonPath("$.status").value("UP"))
            .andExpect(jsonPath("$.app").exists())
            .andExpect(jsonPath("$.version").exists());
    }
}
