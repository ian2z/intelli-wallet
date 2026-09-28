package br.edu.ifpb.pweb2.intelliwallet;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.transaction.annotation.Transactional;

import br.edu.ifpb.pweb2.intelliwallet.model.Comentario;
import br.edu.ifpb.pweb2.intelliwallet.model.ComentarioForm;
import br.edu.ifpb.pweb2.intelliwallet.model.Conta;
import br.edu.ifpb.pweb2.intelliwallet.model.Transacao;
import br.edu.ifpb.pweb2.intelliwallet.repository.ContaRepository;
import br.edu.ifpb.pweb2.intelliwallet.repository.CorrentistaRepository;
import br.edu.ifpb.pweb2.intelliwallet.repository.TransacaoRepository;
import br.edu.ifpb.pweb2.intelliwallet.service.ComentarioJaExisteException;
import br.edu.ifpb.pweb2.intelliwallet.service.ComentarioService;

@SpringBootTest
@Transactional
class ComentarioServiceIntegrationTests {

    @Autowired
    private ComentarioService comentarioService;

    @Autowired
    private CorrentistaRepository correntistaRepository;

    @Autowired
    private ContaRepository contaRepository;

    @Autowired
    private TransacaoRepository transacaoRepository;

    private Transacao buscarTransacaoSeed() {
        Long correntistaId = correntistaRepository.findByLoginIgnoreCase("teste").orElseThrow().getId();
        Conta conta = contaRepository.findByCorrentistaIdAndNumero(correntistaId, "1001-0").orElseThrow();
        return transacaoRepository.findByContaIdOrderByDataDescIdDesc(conta.getId()).get(0);
    }

    private ComentarioForm formularioCom(String texto) {
        ComentarioForm formulario = new ComentarioForm();
        formulario.setTexto(texto);
        return formulario;
    }

    @Test
    void criaComentarioParaTransacaoSemComentario() {
        Transacao transacao = buscarTransacaoSeed();

        Comentario criado = comentarioService.criar(transacao, formularioCom("Primeiro comentário"));

        assertThat(criado.getId()).isNotNull();
        assertThat(comentarioService.buscarPorTransacaoId(transacao.getId())).isPresent();
    }

    @Test
    void lancaExcecaoAoCriarSegundoComentarioParaMesmaTransacao() {
        Transacao transacao = buscarTransacaoSeed();
        comentarioService.criar(transacao, formularioCom("Primeiro comentário"));

        assertThatThrownBy(() -> comentarioService.criar(transacao, formularioCom("Segundo comentário")))
                .isInstanceOf(ComentarioJaExisteException.class);
    }

    @Test
    void atualizaTextoDoComentario() {
        Transacao transacao = buscarTransacaoSeed();
        Comentario comentario = comentarioService.criar(transacao, formularioCom("Texto original"));

        comentarioService.atualizar(comentario, "Texto corrigido");

        assertThat(comentarioService.buscarPorTransacaoId(transacao.getId()).get().getTexto())
                .isEqualTo("Texto corrigido");
    }

    @Test
    void excluiComentario() {
        Transacao transacao = buscarTransacaoSeed();
        Comentario comentario = comentarioService.criar(transacao, formularioCom("Comentário a remover"));

        comentarioService.excluir(comentario);

        assertThat(comentarioService.buscarPorTransacaoId(transacao.getId())).isEmpty();
    }
}
