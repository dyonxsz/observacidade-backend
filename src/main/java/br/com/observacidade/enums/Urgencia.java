package br.com.observacidade.enums;

public enum Urgencia {
    BAIXA("Baixa"),
    MEDIA("Média"),
    ALTA("Alta");

    private final String label;

    Urgencia(String label) {
        this.label = label;
    }

    public String getLabel() {
        return label;
    }
}
