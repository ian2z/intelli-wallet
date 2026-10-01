package br.edu.ifpb.pweb2.intelliwallet.service;

import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.server.ResponseStatusException;

import br.edu.ifpb.pweb2.intelliwallet.model.Correntista;
import br.edu.ifpb.pweb2.intelliwallet.model.Papel;
import br.edu.ifpb.pweb2.intelliwallet.repository.ContaRepository;
import br.edu.ifpb.pweb2.intelliwallet.repository.CorrentistaRepository;
import br.edu.ifpb.pweb2.intelliwallet.repository.TransacaoRepository;
import jakarta.servlet.http.HttpSession;

// Simulação dos papéis na Etapa I. Será substituída pela autenticação na Etapa II.
@Service
public class VisaoService {

    private final CorrentistaRepository correntistaRepository;
    private final ContaRepository contaRepository;
    private final TransacaoRepository transacaoRepository;

    public VisaoService(CorrentistaRepository correntistaRepository, ContaRepository contaRepository,
                        TransacaoRepository transacaoRepository) {
        this.correntistaRepository = correntistaRepository;
        this.contaRepository = contaRepository;
        this.transacaoRepository = transacaoRepository;
    }

    public void selecionarCorrentista(Long id, HttpSession sessao) {
        correntistaRepository.findById(id)
                .filter(pessoa -> pessoa.getPapel() == Papel.CORRENTISTA)
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "Correntista não encontrado"));
        sessao.removeAttribute("administrador");
        sessao.setAttribute("correntistaId", id);
    }

    public void selecionarAdministrador(HttpSession sessao) {
        sessao.removeAttribute("correntistaId");
        sessao.setAttribute("administrador", true);
    }

    public boolean administrador(HttpSession sessao) {
        return Boolean.TRUE.equals(sessao.getAttribute("administrador"));
    }

    public Correntista correntistaAtual(HttpSession sessao) {
        Long id = (Long) sessao.getAttribute("correntistaId");
        if (id == null) {
            return null;
        }
        Correntista correntista = correntistaRepository.findById(id)
                .filter(pessoa -> pessoa.getPapel() == Papel.CORRENTISTA)
                .orElse(null);
        if (correntista == null) {
            sessao.removeAttribute("correntistaId");
        }
        return correntista;
    }

    public Correntista exigirCorrentista(HttpSession sessao) {
        Correntista correntista = correntistaAtual(sessao);
        if (correntista == null) {
            throw new ResponseStatusException(HttpStatus.FORBIDDEN, "Selecione a visão de correntista");
        }
        return correntista;
    }

    public void exigirAdministrador(HttpSession sessao) {
        if (!administrador(sessao)) {
            throw new ResponseStatusException(HttpStatus.FORBIDDEN, "Selecione a visão de administrador");
        }
    }

    @Transactional(readOnly = true)
    public void verificarConta(Long contaId, HttpSession sessao) {
        Long correntistaId = exigirCorrentista(sessao).getId();
        contaRepository.findById(contaId)
                .filter(conta -> conta.getCorrentista().getId().equals(correntistaId))
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "Conta não encontrada"));
    }

    @Transactional(readOnly = true)
    public void verificarTransacao(Long transacaoId, HttpSession sessao) {
        Long correntistaId = exigirCorrentista(sessao).getId();
        transacaoRepository.findById(transacaoId)
                .filter(transacao -> transacao.getConta().getCorrentista().getId().equals(correntistaId))
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "Transação não encontrada"));
    }
}
