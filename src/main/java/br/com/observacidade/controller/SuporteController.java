package br.com.observacidade.controller;

import java.util.List;

import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.data.web.PageableDefault;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import br.com.observacidade.dto.*;
import br.com.observacidade.security.UsuarioAutenticado;
import br.com.observacidade.service.SuporteService;
import jakarta.validation.Valid;

@RestController
@RequestMapping("/suporte")
@CrossOrigin("*")
public class SuporteController {

    private final SuporteService service;
    private final UsuarioAutenticado usuarioAutenticado;

    public SuporteController(SuporteService service, UsuarioAutenticado usuarioAutenticado) {
        this.service = service;
        this.usuarioAutenticado = usuarioAutenticado;
    }

    @PostMapping
    public ResponseEntity<SuporteResponseDTO> criar(@Valid @RequestBody SuporteRequestDTO dto) {
        return ResponseEntity.status(HttpStatus.CREATED)
                .body(service.criar(dto, usuarioAutenticado.email()));
    }

    @GetMapping("/meus")
    public ResponseEntity<List<SuporteResponseDTO>> meus() {
        return ResponseEntity.ok(service.listarDoUsuario(usuarioAutenticado.email()));
    }

    @GetMapping
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<PageResponse<SuporteResponseDTO>> listar(
            @RequestParam(required = false) String status,
            @PageableDefault(size = 20, sort = "dataCriacao", direction = Sort.Direction.DESC) Pageable pageable) {

        return ResponseEntity.ok(service.listar(status, pageable));
    }

    @PutMapping("/{id}")
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<SuporteResponseDTO> responder(
            @PathVariable Long id,
            @Valid @RequestBody SuporteUpdateDTO dto) {

        return ResponseEntity.ok(service.responder(id, dto, usuarioAutenticado.email()));
    }

    @DeleteMapping("/{id}")
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<Void> deletar(@PathVariable Long id) {
        service.deletar(id);
        return ResponseEntity.noContent().build();
    }
}
