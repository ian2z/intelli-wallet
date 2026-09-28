package br.edu.ifpb.pweb2.intelliwallet.model;

public enum Natureza {
    ENTRADA("Entradas"),
    SAIDA("Saídas"),
    INVESTIMENTO("Investimentos");

    private final String descricao;

    Natureza(String descricao) {
        this.descricao = descricao;
    }

    public String getDescricao() {
        return descricao;
    }
}
