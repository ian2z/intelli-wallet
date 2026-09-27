package br.edu.ifpb.pweb2.intelliwallet.service;

public class LoginJaCadastradoException extends RuntimeException {

    public LoginJaCadastradoException() {
        super("Este login já está cadastrado");
    }
}
