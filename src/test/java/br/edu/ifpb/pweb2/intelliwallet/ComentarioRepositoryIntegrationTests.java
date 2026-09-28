package br.edu.ifpb.pweb2.intelliwallet;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.dao.DataIntegrityViolationException;
import org.springframework.transaction.annotation.Transactional;

import br.edu.ifpb.pweb2.intelliwallet.model.Comentario;
import br.edu.ifpb.pweb2.intelliwallet.model.Conta;
import br.edu.ifpb.pweb2.intelliwallet.model.Transacao;
import br.edu.ifpb.pweb2.intelliwallet.repository.ComentarioRepository;
import br.edu.ifpb.pweb2.intelliwallet.repository.ContaRepository;
import br.edu.ifpb.pweb2.intelliwallet.repository.CorrentistaRepository;
import br.edu.ifpb.pweb2.intelliwallet.repository.TransacaoRepository;

@SpringBootTest
@Transactional
class ComentarioRepositoryIntegrationTests {

    @Autowired
    private ComentarioRepository comentarioRepository;

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

    @Test
    void salvaEEncontraComentarioPorTransacaoId() {
        Transacao transacao = buscarTransacaoSeed();
        comentarioRepository.saveAndFlush(new Comentario("Um comentário de teste", transacao));

        assertThat(comentarioRepository.findByTransacaoId(transacao.getId())).isPresent();
    }

    @Test
    void naoPermiteDoisComentariosParaMesmaTransacao() {
        Transacao transacao = buscarTransacaoSeed();
        comentarioRepository.saveAndFlush(new Comentario("Primeiro comentário", transacao));

        Comentario duplicado = new Comentario("Segundo comentário", transacao);
        assertThatThrownBy(() -> comentarioRepository.saveAndFlush(duplicado))
                .isInstanceOf(DataIntegrityViolationException.class);
    }
}
