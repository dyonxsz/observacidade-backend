package br.com.observacidade.enums;

public enum StatusOcorrencia {
    RECEBIDO("Recebida"),
    EM_ANALISE("Em análise"),
    EM_ANDAMENTO("Em andamento"),
    RESOLVIDO("Resolvida");

    private final String label;

    StatusOcorrencia(String label) {
        this.label = label;
    }

    public String getLabel() {
        return label;
    }
}
