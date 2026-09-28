package br.edu.ifpb.pweb2.intelliwallet.service;

import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.stereotype.Service;

@Service
public class SenhaService {

    private final BCryptPasswordEncoder encoder = new BCryptPasswordEncoder();

    public String gerarHash(String senha) {
        return encoder.encode(senha);
    }
}
