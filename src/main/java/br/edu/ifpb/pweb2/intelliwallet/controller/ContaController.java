package br.edu.ifpb.pweb2.intelliwallet.controller;

import org.springframework.core.env.Environment;
import org.springframework.core.env.Profiles;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.ModelAttribute;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;
import org.springframework.validation.BindingResult;
import org.springframework.web.server.ResponseStatusException;

import br.edu.ifpb.pweb2.intelliwallet.model.Correntista;
import br.edu.ifpb.pweb2.intelliwallet.model.ContaForm;
import br.edu.ifpb.pweb2.intelliwallet.model.TipoConta;
import br.edu.ifpb.pweb2.intelliwallet.service.ContaService;
import br.edu.ifpb.pweb2.intelliwallet.service.CorrentistaAtualService;
import br.edu.ifpb.pweb2.intelliwallet.service.NumeroContaJaCadastradoException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.validation.Valid;

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

    @GetMapping("/contas/nova")
    public String formulario(Model model, HttpServletRequest request) {
        if (correntistaAtualService.obterAtual(request).isEmpty()) {
            if (environment.acceptsProfiles(Profiles.of("dev"))) {
                return "redirect:/dev/correntistas";
            }
            throw new ResponseStatusException(HttpStatus.UNAUTHORIZED);
        }
        model.addAttribute("formulario", new ContaForm());
        return "contas/formulario";
    }

    @PostMapping("/contas")
    public String criar(@Valid @ModelAttribute("formulario") ContaForm formulario,
            BindingResult resultado, HttpServletRequest request, RedirectAttributes atributos) {
        Correntista correntista = correntistaAtualService.obterAtual(request)
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.UNAUTHORIZED));
        if (formulario.getTipo() == TipoConta.CARTAO
                && !contaService.diaFechamentoValido(formulario.getDiaFechamento())) {
            resultado.rejectValue("diaFechamento", "dia.invalido",
                    "Informe um dia de fechamento entre 1 e 31");
        }
        if (resultado.hasErrors()) {
            return "contas/formulario";
        }
        try {
            contaService.criar(formulario, correntista.getId());
        } catch (NumeroContaJaCadastradoException ex) {
            resultado.rejectValue("numero", "numero.duplicado", ex.getMessage());
            return "contas/formulario";
        }
        atributos.addFlashAttribute("mensagem", "Conta cadastrada com sucesso.");
        return "redirect:/contas";
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
