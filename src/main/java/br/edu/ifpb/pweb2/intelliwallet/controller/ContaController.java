package br.edu.ifpb.pweb2.intelliwallet.controller;

import java.util.List;

import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.validation.BindingResult;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.ModelAttribute;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;

import br.edu.ifpb.pweb2.intelliwallet.model.Conta;
import br.edu.ifpb.pweb2.intelliwallet.model.ContaForm;
import br.edu.ifpb.pweb2.intelliwallet.model.Correntista;
import br.edu.ifpb.pweb2.intelliwallet.model.TipoConta;
import br.edu.ifpb.pweb2.intelliwallet.model.Transacao;
import br.edu.ifpb.pweb2.intelliwallet.service.ContaInvalidaException;
import br.edu.ifpb.pweb2.intelliwallet.service.ContaService;
import br.edu.ifpb.pweb2.intelliwallet.service.TransacaoService;
import br.edu.ifpb.pweb2.intelliwallet.service.VisaoService;
import jakarta.servlet.http.HttpSession;
import jakarta.validation.Valid;

@Controller
@RequestMapping("/contas")
public class ContaController {

    private final ContaService contaService;
    private final VisaoService visaoService;
    private final TransacaoService transacaoService;

    public ContaController(ContaService contaService,
                           VisaoService visaoService,
                           TransacaoService transacaoService) {
        this.contaService = contaService;
        this.visaoService = visaoService;
        this.transacaoService = transacaoService;
    }

    @GetMapping
    public String listar(HttpSession sessao, Model model) {
        Correntista correntista = visaoService.correntistaAtual(sessao);
        if (correntista == null) {
            return "redirect:/correntista";
        }
        List<Conta> contas = contaService.listarPorCorrentista(correntista.getId());
        model.addAttribute("correntista", correntista);
        model.addAttribute("contas", contas);
        model.addAttribute("saldos", transacaoService.calcularSaldosPorContas(contas));
        return "contas/lista";
    }

    @GetMapping("/nova")
    public String nova(HttpSession sessao, Model model) {
        visaoService.exigirCorrentista(sessao);
        if (!model.containsAttribute("formulario")) {
            model.addAttribute("formulario", new ContaForm());
        }
        model.addAttribute("tipos", TipoConta.values());
        return "contas/formulario";
    }

    @PostMapping
    public String criar(@Valid @ModelAttribute("formulario") ContaForm formulario,
                        BindingResult resultado, HttpSession sessao, RedirectAttributes atributos) {
        Correntista correntista = visaoService.exigirCorrentista(sessao);
        if (!resultado.hasErrors()) {
            try {
                contaService.criar(correntista, formulario);
            } catch (ContaInvalidaException ex) {
                resultado.rejectValue(ex.getCampo(), "conta.invalida", ex.getMessage());
            }
        }
        if (resultado.hasErrors()) {
            atributos.addFlashAttribute("formulario", formulario);
            atributos.addFlashAttribute(BindingResult.MODEL_KEY_PREFIX + "formulario", resultado);
            return "redirect:/contas/nova";
        }
        atributos.addFlashAttribute("mensagem", "Conta cadastrada com sucesso.");
        return "redirect:/contas";
    }

    @GetMapping({"/{id}", "/{id}/transacoes"})
    public String transacoes(@PathVariable Long id, HttpSession sessao, Model model) {
        visaoService.verificarConta(id, sessao);
        Conta conta = transacaoService.buscarConta(id);
        List<Transacao> transacoes = transacaoService.listarPorConta(id);
        model.addAttribute("conta", conta);
        model.addAttribute("transacoes", transacoes);
        model.addAttribute("resumo", transacaoService.calcularResumo(transacoes));
        return "contas/transacoes";
    }
}
