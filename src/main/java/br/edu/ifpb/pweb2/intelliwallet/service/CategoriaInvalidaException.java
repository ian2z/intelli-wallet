package br.edu.ifpb.pweb2.intelliwallet.service;

public class CategoriaInvalidaException extends RuntimeException {

    private final String campo;

    public CategoriaInvalidaException(String campo, String mensagem) {
        super(mensagem);
        this.campo = campo;
    }

    public String getCampo() {
        return campo;
    }
}
