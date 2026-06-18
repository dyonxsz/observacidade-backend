package br.com.observacidade.controller;

import org.springframework.http.*;
import org.springframework.web.bind.MethodArgumentNotValidException;
import org.springframework.web.bind.annotation.*;

@RestControllerAdvice
public class GlobalExceptionHandler {

    @ExceptionHandler(RuntimeException.class)
    public ResponseEntity<String> runtime(RuntimeException ex) {
        String msg = ex.getMessage() == null ? "Erro interno" : ex.getMessage();

        if (msg.toLowerCase().contains("não encontrada") || msg.toLowerCase().contains("não encontrado")) {
            return ResponseEntity.status(HttpStatus.NOT_FOUND).body(msg);
        }

        return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(msg);
    }

    @ExceptionHandler(MethodArgumentNotValidException.class)
    public ResponseEntity<String> validation(MethodArgumentNotValidException ex) {
        return ResponseEntity.badRequest().body("Dados inválidos. Verifique os campos obrigatórios.");
    }
}
