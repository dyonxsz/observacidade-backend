package br.com.observacidade.dto;

import java.time.LocalDateTime;

import br.com.observacidade.entity.HistoricoOcorrencia;
import br.com.observacidade.enums.StatusOcorrencia;

public record HistoricoResponseDTO(
        Long id,
        StatusOcorrencia statusAnterior,
        StatusOcorrencia statusNovo,
        String statusAnteriorLabel,
        String statusNovoLabel,
        String observacao,
        Long usuarioId,
        String usuarioNome,
        LocalDateTime dataCriacao) {

    public static HistoricoResponseDTO from(HistoricoOcorrencia h) {
        return new HistoricoResponseDTO(
                h.getId(),
                h.getStatusAnterior(),
                h.getStatusNovo(),
                h.getStatusAnterior() != null ? h.getStatusAnterior().getLabel() : null,
                h.getStatusNovo() != null ? h.getStatusNovo().getLabel() : null,
                h.getObservacao(),
                h.getUsuarioId(),
                h.getUsuarioNome(),
                h.getDataCriacao());
    }
}
