package br.edu.ifpb.pweb2.intelliwallet.security;

import java.util.Collection;
import java.util.List;

import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.core.userdetails.UserDetails;

import br.edu.ifpb.pweb2.intelliwallet.model.Correntista;
import br.edu.ifpb.pweb2.intelliwallet.model.Papel;

public class CorrentistaUserDetails implements UserDetails {

    private final Long id;
    private final String nome;
    private final String login;
    private final String senha;
    private final Papel papel;
    private final boolean bloqueado;

    public CorrentistaUserDetails(Correntista correntista) {
        this.id = correntista.getId();
        this.nome = correntista.getNome();
        this.login = correntista.getLogin();
        this.senha = correntista.getSenha();
        this.papel = correntista.getPapel();
        this.bloqueado = correntista.isBloqueado();
    }

    public Long getId() {
        return id;
    }

    public String getNome() {
        return nome;
    }

    public Papel getPapel() {
        return papel;
    }

    @Override
    public Collection<? extends GrantedAuthority> getAuthorities() {
        return List.of(new SimpleGrantedAuthority("ROLE_" + papel.name()));
    }

    @Override
    public String getPassword() {
        return senha;
    }

    @Override
    public String getUsername() {
        return login;
    }

    @Override
    public boolean isAccountNonExpired() {
        return true;
    }

    @Override
    public boolean isAccountNonLocked() {
        return !bloqueado;
    }

    @Override
    public boolean isCredentialsNonExpired() {
        return true;
    }

    @Override
    public boolean isEnabled() {
        return true;
    }
}
