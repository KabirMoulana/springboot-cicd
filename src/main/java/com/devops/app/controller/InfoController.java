package com.devops.app.controller;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.Map;

@RestController
@RequestMapping("/api")
public class InfoController {

    @Value("${spring.application.name:springboot-cicd}")
    private String appName;

    @Value("${app.version:1.0.0}")
    private String appVersion;

    @GetMapping("/")
    public ResponseEntity<Map<String, String>> info() {
        return ResponseEntity.ok(Map.of(
            "app", appName,
            "version", appVersion,
            "status", "UP"
        ));
    }
}
