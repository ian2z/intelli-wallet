package br.edu.ifpb.pweb2.intelliwallet.security;

import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import br.edu.ifpb.pweb2.intelliwallet.repository.CorrentistaRepository;

@Service
public class CorrentistaUserDetailsService implements UserDetailsService {

    private final CorrentistaRepository correntistaRepository;

    public CorrentistaUserDetailsService(CorrentistaRepository correntistaRepository) {
        this.correntistaRepository = correntistaRepository;
    }

    @Override
    @Transactional(readOnly = true)
    public UserDetails loadUserByUsername(String username) throws UsernameNotFoundException {
        return correntistaRepository.findByLoginIgnoreCase(username.trim())
                .map(CorrentistaUserDetails::new)
                .orElseThrow(() -> new UsernameNotFoundException("Usuário não encontrado: " + username));
    }
}
