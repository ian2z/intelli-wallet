package br.edu.ifpb.pweb2.intelliwallet.model;

public enum Movimento {
    CREDITO("Crédito"),
    DEBITO("Débito");

    private final String descricao;

    Movimento(String descricao) {
        this.descricao = descricao;
    }

    public String getDescricao() {
        return descricao;
    }
}
