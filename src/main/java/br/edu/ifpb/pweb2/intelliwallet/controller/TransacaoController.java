package br.edu.ifpb.pweb2.intelliwallet.controller;

import br.edu.ifpb.pweb2.intelliwallet.model.*;
import br.edu.ifpb.pweb2.intelliwallet.service.TransacaoInvalidaException;
import br.edu.ifpb.pweb2.intelliwallet.service.TransacaoService;
import jakarta.validation.Valid;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.validation.BindingResult;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;

@Controller
@RequestMapping("/contas/{contaId}/transacoes")
public class TransacaoController {

    private final TransacaoService transacaoService;

    public TransacaoController(TransacaoService transacaoService) {
        this.transacaoService = transacaoService;
    }

    // UC03, passos 2 e 3: correntista clica no "+" e o sistema mostra o formulário
    @GetMapping("/nova")
    public String nova(@PathVariable Long contaId, Model model) {
        model.addAttribute("formulario", new TransacaoForm());
        return exibirFormulario(contaId, model);
    }

    // UC03, passo 4: correntista clica em "Salvar"
    @PostMapping
    public String criar(@PathVariable Long contaId,
                        @Valid @ModelAttribute("formulario") TransacaoForm formulario,
                        BindingResult resultado, Model model, RedirectAttributes atributos) {
        if (resultado.hasErrors()) {
            return exibirFormulario(contaId, model);
        }
        try {
            transacaoService.criar(contaId, formulario);
        } catch (TransacaoInvalidaException ex) {
            if (ex.getCampo() != null) {
                resultado.rejectValue(ex.getCampo(), "transacao.invalida", ex.getMessage());
            } else {
                resultado.reject("transacao.invalida", ex.getMessage());
            }
            return exibirFormulario(contaId, model);
        }
        // P-R-G: depois de salvar, sempre redireciona
        atributos.addFlashAttribute("mensagem", "Transação registrada com sucesso.");
        return "redirect:/contas/" + contaId;
    }

    private String exibirFormulario(Long contaId, Model model) {
        model.addAttribute("conta", transacaoService.buscarConta(contaId));
        model.addAttribute("movimentos", Movimento.values());
        model.addAttribute("categoriasPorNatureza", transacaoService.categoriasAtivasPorNatureza());
        return "transacoes/form";
    }
}