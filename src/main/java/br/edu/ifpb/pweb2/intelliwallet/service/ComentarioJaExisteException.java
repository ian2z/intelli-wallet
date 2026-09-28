package br.edu.ifpb.pweb2.intelliwallet.service;

public class ComentarioJaExisteException extends RuntimeException {

    public ComentarioJaExisteException() {
        super("Esta transação já possui um comentário");
    }
}
