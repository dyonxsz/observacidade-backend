package br.com.observacidade.controller;

import java.util.List;
import java.util.Map;

import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.CrossOrigin;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RestController;

/**
 * Endpoints públicos de boas-vindas e verificação de saúde.
 */
@RestController
@CrossOrigin("*")
public class HomeController {

    @GetMapping("/")
    public ResponseEntity<Map<String, Object>> home() {
        return ResponseEntity.ok(Map.of(
                "aplicacao", "Observa Cidade API",
                "versao", "1.0.0",
                "status", "UP",
                "documentacao", "/swagger-ui.html",
                "dominios", "/dominios"));
    }

    @GetMapping("/health")
    public ResponseEntity<Map<String, String>> health() {
        return ResponseEntity.ok(Map.of("status", "UP"));
    }
}
