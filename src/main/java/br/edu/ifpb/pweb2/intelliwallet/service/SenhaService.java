package br.edu.ifpb.pweb2.intelliwallet.service;

import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;

@Service
public class SenhaService {

    private final PasswordEncoder passwordEncoder;

    public SenhaService(PasswordEncoder passwordEncoder) {
        this.passwordEncoder = passwordEncoder;
    }

    public String gerarHash(String senha) {
        return passwordEncoder.encode(senha);
    }
}
