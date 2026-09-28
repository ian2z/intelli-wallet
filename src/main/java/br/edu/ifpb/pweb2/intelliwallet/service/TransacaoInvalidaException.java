package br.edu.ifpb.pweb2.intelliwallet.service;

// Regra de negócio violada ao registrar uma transação.
// "campo" indica qual campo do formulário mostra o erro (null = erro geral).
public class TransacaoInvalidaException extends RuntimeException {

    private final String campo;

    public TransacaoInvalidaException(String campo, String mensagem) {
        super(mensagem);
        this.campo = campo;
    }

    public String getCampo() {
        return campo;
    }
}
