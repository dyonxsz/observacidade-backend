package br.com.observacidade.controller;

import java.util.Arrays;
import java.util.List;

import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import br.com.observacidade.dto.DominiosResponseDTO;
import br.com.observacidade.dto.OpcaoDTO;
import br.com.observacidade.enums.Categoria;
import br.com.observacidade.enums.StatusOcorrencia;
import br.com.observacidade.enums.Urgencia;

/**
 * Valores permitidos para selects do aplicativo (público, não exige login).
 */
@RestController
@RequestMapping("/dominios")
@CrossOrigin("*")
public class DominioController {

    private static final List<OpcaoDTO> STATUS_SUPORTE = List.of(
            new OpcaoDTO("ABERTO", "Aberto"),
            new OpcaoDTO("EM_ANDAMENTO", "Em andamento"),
            new OpcaoDTO("RESPONDIDO", "Respondido"),
            new OpcaoDTO("FECHADO", "Fechado"));

    @GetMapping
    public ResponseEntity<DominiosResponseDTO> dominios() {
        List<OpcaoDTO> categorias = Arrays.stream(Categoria.values())
                .map(c -> new OpcaoDTO(c.name(), c.getLabel()))
                .toList();

        List<OpcaoDTO> urgencias = Arrays.stream(Urgencia.values())
                .map(u -> new OpcaoDTO(u.name(), u.getLabel()))
                .toList();

        List<OpcaoDTO> status = Arrays.stream(StatusOcorrencia.values())
                .map(s -> new OpcaoDTO(s.name(), s.getLabel()))
                .toList();

        return ResponseEntity.ok(new DominiosResponseDTO(categorias, urgencias, status, STATUS_SUPORTE));
    }
}
