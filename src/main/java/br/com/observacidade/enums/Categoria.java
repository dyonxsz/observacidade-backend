package br.com.observacidade.enums;

public enum Categoria {
    ILUMINACAO("Iluminação pública"),
    SAUDE_PUBLICA("Saúde pública"),
    TRANSITO("Trânsito"),
    INFRAESTRUTURA("Infraestrutura / buracos"),
    LIMPEZA("Limpeza e lixo"),
    SANEAMENTO("Saneamento básico"),
    VAZAMENTO_AGUA("Vazamento de água"),
    TERRENO_ABANDONADO("Terreno abandonado"),
    ESTRADA_RURAL("Estrada rural"),
    SINALIZACAO("Sinalização"),
    OUTROS("Outros");

    private final String label;

    Categoria(String label) {
        this.label = label;
    }

    public String getLabel() {
        return label;
    }
}
