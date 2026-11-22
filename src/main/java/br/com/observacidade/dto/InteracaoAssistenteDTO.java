package br.com.observacidade.dto;

import java.time.LocalDateTime;

public class InteracaoAssistenteDTO {

    private String pergunta;
    private String resposta;
    private String origem;
    private LocalDateTime dataHora;

    public InteracaoAssistenteDTO(String pergunta, String resposta, String origem, LocalDateTime dataHora) {
        this.pergunta = pergunta;
        this.resposta = resposta;
        this.origem = origem;
        this.dataHora = dataHora;
    }

    public String getPergunta() { return pergunta; }
    public String getResposta() { return resposta; }
    public String getOrigem() { return origem; }
    public LocalDateTime getDataHora() { return dataHora; }
}
