package br.edu.ifpb.pweb2.intelliwallet.controller;

import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;

import br.edu.ifpb.pweb2.intelliwallet.service.CorrentistaService;
import br.edu.ifpb.pweb2.intelliwallet.service.VisaoService;
import jakarta.servlet.http.HttpSession;

@Controller
public class VisaoController {

    private final VisaoService visaoService;
    private final CorrentistaService correntistaService;

    public VisaoController(VisaoService visaoService, CorrentistaService correntistaService) {
        this.visaoService = visaoService;
        this.correntistaService = correntistaService;
    }

    @GetMapping("/correntista")
    public String escolherCorrentista(Model model) {
        model.addAttribute("correntistas", correntistaService.listarCorrentistas());
        return "correntistas/selecao";
    }

    @GetMapping("/correntista/{id}")
    public String entrarComoCorrentista(@PathVariable Long id, HttpSession sessao) {
        visaoService.selecionarCorrentista(id, sessao);
        return "redirect:/contas";
    }

    @GetMapping("/admin")
    public String entrarComoAdministrador(HttpSession sessao) {
        visaoService.selecionarAdministrador(sessao);
        return "redirect:/correntistas";
    }

    @PostMapping("/sair")
    public String sair(HttpSession sessao) {
        sessao.invalidate();
        return "redirect:/";
    }
}
