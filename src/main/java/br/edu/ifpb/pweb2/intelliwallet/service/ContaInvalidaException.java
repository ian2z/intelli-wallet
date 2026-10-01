package br.edu.ifpb.pweb2.intelliwallet.service;

public class ContaInvalidaException extends RuntimeException {

    private final String campo;

    public ContaInvalidaException(String campo, String mensagem) {
        super(mensagem);
        this.campo = campo;
    }

    public String getCampo() {
        return campo;
    }
}
