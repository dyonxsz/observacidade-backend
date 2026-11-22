package br.com.observacidade.controller;

import java.util.List;

import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.data.web.PageableDefault;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

import br.com.observacidade.dto.*;
import br.com.observacidade.enums.*;
import br.com.observacidade.security.UsuarioAutenticado;
import br.com.observacidade.service.OcorrenciaService;
import jakarta.validation.Valid;

@RestController
@RequestMapping("/ocorrencias")
@CrossOrigin("*")
public class OcorrenciaController {

    private final OcorrenciaService service;
    private final UsuarioAutenticado usuarioAutenticado;

    public OcorrenciaController(OcorrenciaService service, UsuarioAutenticado usuarioAutenticado) {
        this.service = service;
        this.usuarioAutenticado = usuarioAutenticado;
    }

    @PostMapping
    public ResponseEntity<OcorrenciaResponseDTO> criar(@Valid @RequestBody OcorrenciaRequestDTO dto) {
        return ResponseEntity.status(HttpStatus.CREATED)
                .body(service.criar(dto, usuarioAutenticado.email()));
    }

    @GetMapping
    public ResponseEntity<PageResponse<OcorrenciaResumoDTO>> listar(
            @RequestParam(required = false) StatusOcorrencia status,
            @RequestParam(required = false) Categoria categoria,
            @RequestParam(required = false) Urgencia urgencia,
            @RequestParam(required = false) String bairro,
            @RequestParam(required = false) String busca,
            @PageableDefault(size = 20, sort = "dataCriacao", direction = Sort.Direction.DESC) Pageable pageable) {

        return ResponseEntity.ok(service.listar(status, categoria, urgencia, bairro, busca, pageable));
    }

    @GetMapping("/minhas")
    public ResponseEntity<PageResponse<OcorrenciaResumoDTO>> minhas(
            @PageableDefault(size = 20, sort = "dataCriacao", direction = Sort.Direction.DESC) Pageable pageable) {

        return ResponseEntity.ok(service.listarDoUsuario(usuarioAutenticado.email(), pageable));
    }

    @GetMapping("/minhas/estatisticas")
    public ResponseEntity<EstatisticasDTO> minhasEstatisticas() {
        return ResponseEntity.ok(service.estatisticasDoUsuario(usuarioAutenticado.email()));
    }

    @GetMapping("/estatisticas")
    public ResponseEntity<EstatisticasDTO> estatisticas() {
        return ResponseEntity.ok(service.estatisticas());
    }

    @GetMapping("/{id}")
    public ResponseEntity<OcorrenciaResponseDTO> buscar(
            @PathVariable Long id,
            @RequestParam(defaultValue = "false") boolean incluirImagem) {

        return ResponseEntity.ok(service.buscarDetalhe(id, incluirImagem));
    }

    @GetMapping("/{id}/imagem")
    public ResponseEntity<byte[]> imagem(@PathVariable Long id) {
        OcorrenciaService.ImagemArquivo imagem = service.obterImagem(id);

        return ResponseEntity.ok()
                .contentType(MediaType.parseMediaType(imagem.contentType()))
                .header(HttpHeaders.CACHE_CONTROL, "public, max-age=3600")
                .body(imagem.bytes());
    }

    @PostMapping(value = "/{id}/imagem", consumes = MediaType.MULTIPART_FORM_DATA_VALUE)
    public ResponseEntity<ImagemResponseDTO> enviarImagem(
            @PathVariable Long id,
            @RequestParam("arquivo") MultipartFile arquivo) {

        return ResponseEntity.ok(service.atualizarImagem(id, arquivo, usuarioAutenticado.email()));
    }

    @PutMapping("/{id}")
    public ResponseEntity<OcorrenciaResponseDTO> atualizar(
            @PathVariable Long id,
            @Valid @RequestBody OcorrenciaRequestDTO dto) {

        return ResponseEntity.ok(service.atualizar(id, dto, usuarioAutenticado.email()));
    }

    @PutMapping("/{id}/status")
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<OcorrenciaResponseDTO> atualizarStatus(
            @PathVariable Long id,
            @Valid @RequestBody StatusUpdateDTO dto) {

        return ResponseEntity.ok(service.atualizarStatus(id, dto, usuarioAutenticado.email()));
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<Void> deletar(@PathVariable Long id) {
        service.deletar(id, usuarioAutenticado.email());
        return ResponseEntity.noContent().build();
    }
}
