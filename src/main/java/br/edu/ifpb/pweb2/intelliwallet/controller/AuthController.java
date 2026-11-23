package br.edu.ifpb.pweb2.intelliwallet.controller;

import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.GetMapping;

import br.edu.ifpb.pweb2.intelliwallet.service.VisaoService;

@Controller
public class AuthController {

    private final VisaoService visaoService;

    public AuthController(VisaoService visaoService) {
        this.visaoService = visaoService;
    }

    @GetMapping("/login")
    public String login() {
        if (visaoService.correntistaAtual() != null) {
            return visaoService.administrador() ? "redirect:/correntistas" : "redirect:/contas";
        }
        return "auth/login";
    }

    @GetMapping("/acesso-negado")
    public String acessoNegado() {
        return "auth/acesso-negado";
    }
}
