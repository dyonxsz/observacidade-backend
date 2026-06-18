package br.com.observacidade.controller;

import java.security.Principal;
import java.util.List;

import org.springframework.http.*;
import org.springframework.web.bind.annotation.*;

import br.com.observacidade.dto.*;
import br.com.observacidade.service.OcorrenciaService;
import br.com.observacidade.entity.*;
import br.com.observacidade.repository.NotificacaoRepository;
import jakarta.validation.Valid;

@RestController
@RequestMapping("/ocorrencias")
@CrossOrigin("*")
public class OcorrenciaController {

    private final OcorrenciaService service;
    private final NotificacaoRepository notificacaoRepository;

    public OcorrenciaController(OcorrenciaService service, NotificacaoRepository notificacaoRepository) {
        this.service = service;
        this.notificacaoRepository = notificacaoRepository;
    }

    @PostMapping
    public ResponseEntity<OcorrenciaResponseDTO> criar(
            @Valid @RequestBody OcorrenciaRequestDTO dto,
            Principal principal) {

        return ResponseEntity
                .status(HttpStatus.CREATED)
                .body(new OcorrenciaResponseDTO(service.salvar(dto, principal.getName())));
    }

    @GetMapping
    public ResponseEntity<List<OcorrenciaResponseDTO>> listar() {
        List<OcorrenciaResponseDTO> resposta = service.listar()
                .stream()
                .map(OcorrenciaResponseDTO::new)
                .toList();

        return ResponseEntity.ok(resposta);
    }

    @GetMapping("/{id}")
    public ResponseEntity<OcorrenciaResponseDTO> buscar(@PathVariable Long id) {
        return ResponseEntity.ok(new OcorrenciaResponseDTO(service.buscar(id)));
    }

    @PutMapping("/{id}")
    public ResponseEntity<OcorrenciaResponseDTO> atualizar(
            @PathVariable Long id,
            @Valid @RequestBody OcorrenciaRequestDTO dto) {

        return ResponseEntity.ok(new OcorrenciaResponseDTO(service.atualizar(id, dto)));
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<Void> deletar(@PathVariable Long id) {
        service.deletar(id);
        return ResponseEntity.noContent().build();
    }
    @PutMapping("/{id}/status")
    public ResponseEntity<OcorrenciaResponseDTO> atualizarStatus(
            @PathVariable Long id,
            @RequestBody StatusUpdateDTO dto) {

        Ocorrencia ocorrencia = service.buscar(id);
        ocorrencia.setStatus(dto.getStatus());

        Ocorrencia atualizada = service.salvarEntidade(ocorrencia);

        if (atualizada.getUsuario() != null) {
            Notificacao n = new Notificacao();
            n.setUsuario(atualizada.getUsuario());
            n.setOcorrencia(atualizada);
            n.setTipo(dto.getStatus() != null ? dto.getStatus().name() : "STATUS");
            n.setTitulo("Status da ocorrência atualizado");
            n.setMensagem("Sua ocorrência agora está como: " + dto.getStatus());
            notificacaoRepository.save(n);
        }

        return ResponseEntity.ok(new OcorrenciaResponseDTO(atualizada));
    }

}
