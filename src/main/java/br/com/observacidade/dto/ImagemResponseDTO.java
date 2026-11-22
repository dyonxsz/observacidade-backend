package br.com.observacidade.dto;

public record ImagemResponseDTO(String imagemBase64, String contentType, Integer tamanhoBytes) {
}
