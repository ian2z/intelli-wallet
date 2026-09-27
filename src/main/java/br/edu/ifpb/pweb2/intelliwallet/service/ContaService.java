package br.edu.ifpb.pweb2.intelliwallet.service;

import java.util.List;

import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.server.ResponseStatusException;

import br.edu.ifpb.pweb2.intelliwallet.model.Conta;
import br.edu.ifpb.pweb2.intelliwallet.model.Transacao;
import br.edu.ifpb.pweb2.intelliwallet.repository.ContaRepository;
import br.edu.ifpb.pweb2.intelliwallet.repository.TransacaoRepository;

@Service
public class ContaService {

    private final ContaRepository contaRepository;
    private final TransacaoRepository transacaoRepository;

    public ContaService(ContaRepository contaRepository, TransacaoRepository transacaoRepository) {
        this.contaRepository = contaRepository;
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
}
