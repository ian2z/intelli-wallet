package br.edu.ifpb.pweb2.intelliwallet.service;

import java.util.List;
import java.util.Locale;

import org.springframework.dao.DataIntegrityViolationException;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.server.ResponseStatusException;

import br.edu.ifpb.pweb2.intelliwallet.model.CadastroCorrentistaForm;
import br.edu.ifpb.pweb2.intelliwallet.model.Correntista;
import br.edu.ifpb.pweb2.intelliwallet.model.CorrentistaForm;
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

    @Transactional(readOnly = true)
    public List<Correntista> listarCorrentistas() {
        return correntistaRepository.findByPapelOrderByNomeAsc(Papel.CORRENTISTA);
    }

    @Transactional(readOnly = true)
    public List<Correntista> listarTodos() {
        return listarCorrentistas();
    }

    @Transactional(readOnly = true)
    public Correntista buscarPorId(Long id) {
        return correntistaRepository.findById(id)
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "Correntista não encontrado"));
    }

    @Transactional
    public Correntista criar(CorrentistaForm formulario) {
        String login = formulario.getLogin().trim().toLowerCase(Locale.ROOT);
        if (correntistaRepository.existsByLoginIgnoreCase(login)) {
            throw new LoginJaCadastradoException();
        }

        Correntista correntista = new Correntista();
        correntista.setNome(formulario.getNome().trim());
        correntista.setLogin(login);
        correntista.setSenha(senhaService.gerarHash(formulario.getSenha()));
        correntista.setPapel(formulario.getPapel() != null ? formulario.getPapel() : Papel.CORRENTISTA);
        correntista.setBloqueado(formulario.isBloqueado());
        try {
            return correntistaRepository.saveAndFlush(correntista);
        } catch (DataIntegrityViolationException ex) {
            throw new LoginJaCadastradoException();
        }
    }

    @Transactional
    public Correntista atualizar(Long id, CorrentistaForm formulario) {
        Correntista correntista = buscarPorId(id);
        String novoLogin = formulario.getLogin().trim().toLowerCase(Locale.ROOT);

        if (!correntista.getLogin().equalsIgnoreCase(novoLogin)
                && correntistaRepository.existsByLoginIgnoreCase(novoLogin)) {
            throw new LoginJaCadastradoException();
        }

        correntista.setNome(formulario.getNome().trim());
        correntista.setLogin(novoLogin);
        correntista.setBloqueado(formulario.isBloqueado());
        if (formulario.getPapel() != null) {
            correntista.setPapel(formulario.getPapel());
        }

        if (formulario.getSenha() != null && !formulario.getSenha().isBlank()) {
            correntista.setSenha(senhaService.gerarHash(formulario.getSenha()));
        }

        try {
            return correntistaRepository.saveAndFlush(correntista);
        } catch (DataIntegrityViolationException ex) {
            throw new LoginJaCadastradoException();
        }
    }

    @Transactional
    public void excluir(Long id) {
        Correntista correntista = buscarPorId(id);
        correntistaRepository.delete(correntista);
        correntistaRepository.flush();
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

