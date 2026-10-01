package br.edu.ifpb.pweb2.intelliwallet.controller;

import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.validation.BindingResult;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.ModelAttribute;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;

import br.edu.ifpb.pweb2.intelliwallet.model.Movimento;
import br.edu.ifpb.pweb2.intelliwallet.model.TransacaoForm;
import br.edu.ifpb.pweb2.intelliwallet.service.TransacaoInvalidaException;
import br.edu.ifpb.pweb2.intelliwallet.service.TransacaoService;
import br.edu.ifpb.pweb2.intelliwallet.service.VisaoService;
import jakarta.servlet.http.HttpSession;
import jakarta.validation.Valid;

@Controller
@RequestMapping("/contas/{contaId}/transacoes")
public class TransacaoController {

    private final TransacaoService transacaoService;
    private final VisaoService visaoService;

    public TransacaoController(TransacaoService transacaoService, VisaoService visaoService) {
        this.transacaoService = transacaoService;
        this.visaoService = visaoService;
    }

    @ModelAttribute
    public void verificarVisao(@PathVariable Long contaId, HttpSession sessao) {
        visaoService.verificarConta(contaId, sessao);
    }

    // UC03, passos 2 e 3: correntista clica no "+" e o sistema mostra o formulário
    @GetMapping("/nova")
    public String nova(@PathVariable Long contaId, Model model) {
        model.addAttribute("formulario", new TransacaoForm());
        return exibirFormulario(contaId, null, model);
    }

    // UC03, passo 4: correntista clica em "Salvar"
    @PostMapping
    public String criar(@PathVariable Long contaId,
                        @Valid @ModelAttribute("formulario") TransacaoForm formulario,
                        BindingResult resultado, Model model, RedirectAttributes atributos) {
        if (resultado.hasErrors()) {
            return exibirFormulario(contaId, null, model);
        }
        try {
            transacaoService.criar(contaId, formulario);
        } catch (TransacaoInvalidaException ex) {
            registrarErro(resultado, ex);
            return exibirFormulario(contaId, null, model);
        }
        // P-R-G: depois de salvar, sempre redireciona
        atributos.addFlashAttribute("mensagem", "Transação registrada com sucesso.");
        return "redirect:/contas/" + contaId;
    }

    // UC04, passos 2 e 3: formulário com os dados atuais da transação
    @GetMapping("/{transacaoId}/editar")
    public String editar(@PathVariable Long contaId, @PathVariable Long transacaoId, Model model) {
        model.addAttribute("formulario", transacaoService.formularioDeEdicao(contaId, transacaoId));
        return exibirFormulario(contaId, transacaoId, model);
    }

    // UC04, passos 4 e 5: correntista modifica os dados e clica em "Salvar"
    @PostMapping("/{transacaoId}")
    public String atualizar(@PathVariable Long contaId, @PathVariable Long transacaoId,
                            @Valid @ModelAttribute("formulario") TransacaoForm formulario,
                            BindingResult resultado, Model model, RedirectAttributes atributos) {
        if (resultado.hasErrors()) {
            return exibirFormulario(contaId, transacaoId, model);
        }
        try {
            transacaoService.atualizar(contaId, transacaoId, formulario);
        } catch (TransacaoInvalidaException ex) {
            registrarErro(resultado, ex);
            return exibirFormulario(contaId, transacaoId, model);
        }
        atributos.addFlashAttribute("mensagem", "Transação atualizada com sucesso.");
        return "redirect:/contas/" + contaId;
    }

    // Mesmo formulário para criar (transacaoId == null) e editar (UC04 reaproveita o UC03)
    private String exibirFormulario(Long contaId, Long transacaoId, Model model) {
        model.addAttribute("conta", transacaoService.buscarConta(contaId));
        model.addAttribute("transacaoId", transacaoId);
        model.addAttribute("movimentos", Movimento.values());
        model.addAttribute("categoriasPorNatureza", transacaoId == null
                ? transacaoService.categoriasAtivasPorNatureza()
                : transacaoService.categoriasPorNatureza(transacaoService.categoriaAtual(contaId, transacaoId)));
        return "transacoes/form";
    }

    private void registrarErro(BindingResult resultado, TransacaoInvalidaException ex) {
        if (ex.getCampo() != null) {
            resultado.rejectValue(ex.getCampo(), "transacao.invalida", ex.getMessage());
        } else {
            resultado.reject("transacao.invalida", ex.getMessage());
        }
    }
}
