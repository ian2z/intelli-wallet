package br.edu.ifpb.pweb2.intelliwallet.service;

import org.springframework.http.HttpStatus;
import org.springframework.security.authentication.AnonymousAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.server.ResponseStatusException;

import br.edu.ifpb.pweb2.intelliwallet.model.Correntista;
import br.edu.ifpb.pweb2.intelliwallet.model.Papel;
import br.edu.ifpb.pweb2.intelliwallet.repository.ContaRepository;
import br.edu.ifpb.pweb2.intelliwallet.repository.CorrentistaRepository;
import br.edu.ifpb.pweb2.intelliwallet.repository.TransacaoRepository;

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

    public boolean administrador() {
        Authentication auth = autenticacaoAtual();
        if (auth == null) {
            return false;
        }
        return auth.getAuthorities().stream()
                .anyMatch(a -> "ROLE_ADMIN".equals(a.getAuthority()));
    }

    @Transactional(readOnly = true)
    public Correntista correntistaAtual() {
        Authentication auth = autenticacaoAtual();
        if (auth == null) {
            return null;
        }
        return correntistaRepository.findByLoginIgnoreCase(auth.getName()).orElse(null);
    }

    @Transactional(readOnly = true)
    public Correntista exigirCorrentista() {
        Correntista usuario = correntistaAtual();
        if (usuario == null) {
            throw new ResponseStatusException(HttpStatus.FORBIDDEN, "Autenticação necessária");
        }
        return usuario;
    }

    public void exigirAdministrador() {
        if (!administrador()) {
            throw new ResponseStatusException(HttpStatus.FORBIDDEN, "Acesso restrito ao administrador");
        }
    }

    @Transactional(readOnly = true)
    public void verificarConta(Long contaId) {
        Correntista usuario = exigirCorrentista();
        boolean admin = usuario.getPapel() == Papel.ADMIN || administrador();
        contaRepository.findById(contaId)
                .filter(conta -> admin || conta.getCorrentista().getId().equals(usuario.getId()))
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "Conta não encontrada"));
    }

    @Transactional(readOnly = true)
    public void verificarTransacao(Long transacaoId) {
        Correntista usuario = exigirCorrentista();
        boolean admin = usuario.getPapel() == Papel.ADMIN || administrador();
        transacaoRepository.findById(transacaoId)
                .filter(transacao -> admin || transacao.getConta().getCorrentista().getId().equals(usuario.getId()))
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "Transação não encontrada"));
    }

    private Authentication autenticacaoAtual() {
        Authentication auth = SecurityContextHolder.getContext().getAuthentication();
        if (auth == null || !auth.isAuthenticated() || auth instanceof AnonymousAuthenticationToken) {
            return null;
        }
        return auth;
    }
}
