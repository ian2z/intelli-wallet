package br.edu.ifpb.pweb2.intelliwallet.controller;

import org.springframework.core.env.Environment;
import org.springframework.core.env.Profiles;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.server.ResponseStatusException;

import br.edu.ifpb.pweb2.intelliwallet.model.Correntista;
import br.edu.ifpb.pweb2.intelliwallet.service.ContaService;
import br.edu.ifpb.pweb2.intelliwallet.service.CorrentistaAtualService;
import jakarta.servlet.http.HttpServletRequest;

@Controller
public class ContaController {

    private final ContaService contaService;
    private final CorrentistaAtualService correntistaAtualService;
    private final Environment environment;

    public ContaController(ContaService contaService, CorrentistaAtualService correntistaAtualService,
            Environment environment) {
        this.contaService = contaService;
        this.correntistaAtualService = correntistaAtualService;
        this.environment = environment;
    }

    @GetMapping("/contas")
    public String listar(Model model, HttpServletRequest request) {
        var atual = correntistaAtualService.obterAtual(request);
        if (atual.isEmpty() && environment.acceptsProfiles(Profiles.of("dev"))) {
            return "redirect:/dev/correntistas";
        }
        Correntista correntista = atual.orElseThrow(
                () -> new ResponseStatusException(HttpStatus.UNAUTHORIZED));
        model.addAttribute("correntista", correntista);
        model.addAttribute("modoDev", environment.acceptsProfiles(Profiles.of("dev")));
        model.addAttribute("contas", contaService.listarDoCorrentista(correntista.getId()));
        return "contas/lista";
    }

    @GetMapping("/contas/{id}/transacoes")
    public String transacoes(@PathVariable Long id, Model model, HttpServletRequest request) {
        Correntista correntista = correntistaAtualService.obterAtual(request)
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.UNAUTHORIZED));
        model.addAttribute("conta", contaService.buscarDoCorrentista(id, correntista.getId()));
        model.addAttribute("transacoes", contaService.listarTransacoes(id, correntista.getId()));
        return "contas/transacoes";
    }
}
