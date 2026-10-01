package br.edu.ifpb.pweb2.intelliwallet.controller;

import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.ControllerAdvice;
import org.springframework.web.bind.annotation.ModelAttribute;

import br.edu.ifpb.pweb2.intelliwallet.service.VisaoService;
import jakarta.servlet.http.HttpSession;

@ControllerAdvice
public class VisaoModelAdvice {

    private final VisaoService visaoService;

    public VisaoModelAdvice(VisaoService visaoService) {
        this.visaoService = visaoService;
    }

    @ModelAttribute
    public void visaoAtual(HttpSession sessao, Model model) {
        model.addAttribute("administrador", visaoService.administrador(sessao));
        model.addAttribute("correntistaAtual", visaoService.correntistaAtual(sessao));
    }
}
