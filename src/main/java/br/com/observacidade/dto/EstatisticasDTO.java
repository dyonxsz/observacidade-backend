package br.com.observacidade.dto;

import java.util.Map;

public record EstatisticasDTO(
        long total,
        Map<String, Long> porStatus,
        Map<String, Long> porCategoria,
        Map<String, Long> porUrgencia) {
}
