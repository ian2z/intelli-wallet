package br.edu.ifpb.pweb2.intelliwallet.service;

import java.util.List;

import org.springframework.dao.DataIntegrityViolationException;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import br.edu.ifpb.pweb2.intelliwallet.model.Conta;
import br.edu.ifpb.pweb2.intelliwallet.model.ContaForm;
import br.edu.ifpb.pweb2.intelliwallet.model.Correntista;
import br.edu.ifpb.pweb2.intelliwallet.model.TipoConta;
import br.edu.ifpb.pweb2.intelliwallet.repository.ContaRepository;

@Service
public class ContaService {

    private final ContaRepository contaRepository;

    public ContaService(ContaRepository contaRepository) {
        this.contaRepository = contaRepository;
    }

    @Transactional(readOnly = true)
    public List<Conta> listarPorCorrentista(Long correntistaId) {
        return contaRepository.findByCorrentistaIdOrderByIdAsc(correntistaId);
    }

    @Transactional
    public Conta criar(Correntista correntista, ContaForm formulario) {
        if (correntista.isBloqueado()) {
            throw new ContaInvalidaException("numero", "Este correntista está bloqueado e não pode criar contas");
        }
        if (formulario.getTipo() == TipoConta.CARTAO && formulario.getDiaFechamento() == null) {
            throw new ContaInvalidaException("diaFechamento", "Informe o dia de fechamento do cartão");
        }
        String numero = formulario.getNumero().trim();
        if (contaRepository.findByCorrentistaIdAndNumero(correntista.getId(), numero).isPresent()) {
            throw new ContaInvalidaException("numero", "Você já possui uma conta com este número");
        }

        Conta conta = new Conta();
        conta.setCorrentista(correntista);
        conta.setNumero(numero);
        conta.setDescricao(formulario.getDescricao().trim());
        conta.setTipo(formulario.getTipo());
        conta.setDiaFechamento(formulario.getTipo() == TipoConta.CARTAO ? formulario.getDiaFechamento() : null);
        try {
            return contaRepository.saveAndFlush(conta);
        } catch (DataIntegrityViolationException ex) {
            throw new ContaInvalidaException("numero", "Você já possui uma conta com este número");
        }
    }
}
