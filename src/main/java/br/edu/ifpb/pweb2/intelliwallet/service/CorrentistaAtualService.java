package br.edu.ifpb.pweb2.intelliwallet.service;

import java.util.Optional;

import org.springframework.core.env.Environment;
import org.springframework.core.env.Profiles;
import org.springframework.stereotype.Service;

import br.edu.ifpb.pweb2.intelliwallet.model.Correntista;
import br.edu.ifpb.pweb2.intelliwallet.repository.CorrentistaRepository;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpSession;

@Service
public class CorrentistaAtualService {

    public static final String CHAVE_SESSAO = "correntistaId";

    private final CorrentistaRepository correntistaRepository;
    private final Environment environment;

    public CorrentistaAtualService(CorrentistaRepository correntistaRepository, Environment environment) {
        this.correntistaRepository = correntistaRepository;
        this.environment = environment;
    }

    public Optional<Correntista> obterAtual(HttpServletRequest request) {
        if (!environment.acceptsProfiles(Profiles.of("dev"))) {
            return Optional.empty();
        }
        HttpSession sessao = request.getSession(false);
        if (sessao == null || !(sessao.getAttribute(CHAVE_SESSAO) instanceof Long id)) {
            return Optional.empty();
        }
        return correntistaRepository.findById(id).filter(correntista -> !correntista.isBloqueado());
    }
}
