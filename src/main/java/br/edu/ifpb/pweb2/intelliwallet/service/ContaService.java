package br.edu.ifpb.pweb2.intelliwallet.service;

import java.util.List;

import org.springframework.http.HttpStatus;
import org.springframework.dao.DataIntegrityViolationException;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.server.ResponseStatusException;

import br.edu.ifpb.pweb2.intelliwallet.model.Conta;
import br.edu.ifpb.pweb2.intelliwallet.model.ContaForm;
import br.edu.ifpb.pweb2.intelliwallet.model.Correntista;
import br.edu.ifpb.pweb2.intelliwallet.model.TipoConta;
import br.edu.ifpb.pweb2.intelliwallet.model.Transacao;
import br.edu.ifpb.pweb2.intelliwallet.repository.ContaRepository;
import br.edu.ifpb.pweb2.intelliwallet.repository.CorrentistaRepository;
import br.edu.ifpb.pweb2.intelliwallet.repository.TransacaoRepository;

@Service
public class ContaService {

    private final ContaRepository contaRepository;
    private final CorrentistaRepository correntistaRepository;
    private final TransacaoRepository transacaoRepository;

    public ContaService(ContaRepository contaRepository, CorrentistaRepository correntistaRepository,
            TransacaoRepository transacaoRepository) {
        this.contaRepository = contaRepository;
        this.correntistaRepository = correntistaRepository;
        this.transacaoRepository = transacaoRepository;
    }

    @Transactional(readOnly = true)
    public List<Conta> listarDoCorrentista(Long correntistaId) {
        return contaRepository.findByCorrentistaIdOrderByIdAsc(correntistaId);
    }

    @Transactional(readOnly = true)
    public Conta buscarDoCorrentista(Long contaId, Long correntistaId) {
        return contaRepository.findByIdAndCorrentistaId(contaId, correntistaId)
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND));
    }

    @Transactional(readOnly = true)
    public List<Transacao> listarTransacoes(Long contaId, Long correntistaId) {
        buscarDoCorrentista(contaId, correntistaId);
        return transacaoRepository.findByContaIdOrderByDataDescIdDesc(contaId);
    }

    @Transactional
    public Conta criar(ContaForm formulario, Long correntistaId) {
        Correntista correntista = correntistaRepository.findById(correntistaId)
                .filter(pessoa -> !pessoa.isBloqueado())
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.FORBIDDEN));
        String numero = formulario.getNumero().trim();
        if (contaRepository.existsByCorrentistaIdAndNumeroIgnoreCase(correntistaId, numero)) {
            throw new NumeroContaJaCadastradoException();
        }
        if (formulario.getTipo() == TipoConta.CARTAO && !diaFechamentoValido(formulario.getDiaFechamento())) {
            throw new IllegalArgumentException("Informe um dia de fechamento entre 1 e 31");
        }

        Conta conta = new Conta();
        conta.setCorrentista(correntista);
        conta.setNumero(numero);
        conta.setDescricao(formulario.getDescricao().trim());
        conta.setTipo(formulario.getTipo());
        conta.setDiaFechamento(formulario.getTipo() == TipoConta.CARTAO
                ? formulario.getDiaFechamento() : null);
        try {
            return contaRepository.saveAndFlush(conta);
        } catch (DataIntegrityViolationException ex) {
            throw new NumeroContaJaCadastradoException();
        }
    }

    public boolean diaFechamentoValido(Integer dia) {
        return dia != null && dia >= 1 && dia <= 31;
    }
}
