package br.edu.ifpb.pweb2.intelliwallet.service;

public class NumeroContaJaCadastradoException extends RuntimeException {

    public NumeroContaJaCadastradoException() {
        super("Você já possui uma conta com esse número");
    }
}
