package br.com.observacidade.dto;

import java.util.List;
import java.util.Map;

public class AssistenteEstatisticasDTO {

    private long totalPerguntas;
    private Map<String, Long> porOrigem;
    private List<String> semRespostaRecentes;

    public AssistenteEstatisticasDTO(long totalPerguntas, Map<String, Long> porOrigem,
            List<String> semRespostaRecentes) {
        this.totalPerguntas = totalPerguntas;
        this.porOrigem = porOrigem;
        this.semRespostaRecentes = semRespostaRecentes;
    }

    public long getTotalPerguntas() { return totalPerguntas; }
    public Map<String, Long> getPorOrigem() { return porOrigem; }
    public List<String> getSemRespostaRecentes() { return semRespostaRecentes; }
}
