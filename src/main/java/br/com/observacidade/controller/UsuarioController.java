package br.com.observacidade.controller;

import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.data.web.PageableDefault;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import br.com.observacidade.dto.PageResponse;
import br.com.observacidade.dto.UsuarioResponseDTO;
import br.com.observacidade.security.UsuarioAutenticado;
import br.com.observacidade.service.UsuarioService;

@RestController
@RequestMapping("/usuarios")
@CrossOrigin("*")
@PreAuthorize("hasRole('ADMIN')")
public class UsuarioController {

    private final UsuarioService service;
    private final UsuarioAutenticado usuarioAutenticado;

    public UsuarioController(UsuarioService service, UsuarioAutenticado usuarioAutenticado) {
        this.service = service;
        this.usuarioAutenticado = usuarioAutenticado;
    }

    @GetMapping
    public ResponseEntity<PageResponse<UsuarioResponseDTO>> listar(
            @PageableDefault(size = 20, sort = "nome", direction = Sort.Direction.ASC) Pageable pageable) {

        return ResponseEntity.ok(service.listar(pageable));
    }

    @GetMapping("/{id}")
    public ResponseEntity<UsuarioResponseDTO> buscar(@PathVariable Long id) {
        return ResponseEntity.ok(service.buscar(id));
    }

    @PutMapping("/{id}/admin")
    public ResponseEntity<UsuarioResponseDTO> tornarAdmin(@PathVariable Long id) {
        return ResponseEntity.ok(service.tornarAdmin(id));
    }

    @PutMapping("/{id}/user")
    public ResponseEntity<UsuarioResponseDTO> tornarUsuario(@PathVariable Long id) {
        return ResponseEntity.ok(service.tornarUsuario(id, usuarioAutenticado.email()));
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<Void> deletar(@PathVariable Long id) {
        service.deletar(id, usuarioAutenticado.email());
        return ResponseEntity.noContent().build();
    }
}
