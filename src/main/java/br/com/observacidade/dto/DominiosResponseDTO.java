package br.com.observacidade.dto;

import java.util.List;

public record DominiosResponseDTO(
        List<OpcaoDTO> categorias,
        List<OpcaoDTO> urgencias,
        List<OpcaoDTO> status,
        List<OpcaoDTO> statusSuporte) {
}
