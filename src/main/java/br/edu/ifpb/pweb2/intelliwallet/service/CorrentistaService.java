package br.edu.ifpb.pweb2.intelliwallet.service;

import java.util.Locale;

import org.springframework.dao.DataIntegrityViolationException;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import br.edu.ifpb.pweb2.intelliwallet.model.CadastroCorrentistaForm;
import br.edu.ifpb.pweb2.intelliwallet.model.Correntista;
import br.edu.ifpb.pweb2.intelliwallet.model.Papel;
import br.edu.ifpb.pweb2.intelliwallet.repository.CorrentistaRepository;

@Service
public class CorrentistaService {

    private final CorrentistaRepository correntistaRepository;
    private final SenhaService senhaService;

    public CorrentistaService(CorrentistaRepository correntistaRepository, SenhaService senhaService) {
        this.correntistaRepository = correntistaRepository;
        this.senhaService = senhaService;
    }

    @Transactional
    public Correntista cadastrar(CadastroCorrentistaForm formulario) {
        String login = formulario.getLogin().trim().toLowerCase(Locale.ROOT);
        if (correntistaRepository.existsByLoginIgnoreCase(login)) {
            throw new LoginJaCadastradoException();
        }

        Correntista correntista = new Correntista();
        correntista.setNome(formulario.getNome().trim());
        correntista.setLogin(login);
        correntista.setSenha(senhaService.gerarHash(formulario.getSenha()));
        correntista.setPapel(Papel.CORRENTISTA);
        try {
            return correntistaRepository.saveAndFlush(correntista);
        } catch (DataIntegrityViolationException ex) {
            throw new LoginJaCadastradoException();
        }
    }
}
