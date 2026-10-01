package br.edu.ifpb.pweb2.intelliwallet.controller;

import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.validation.BindingResult;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.ModelAttribute;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;

import br.edu.ifpb.pweb2.intelliwallet.model.Correntista;
import br.edu.ifpb.pweb2.intelliwallet.model.CorrentistaForm;
import br.edu.ifpb.pweb2.intelliwallet.service.CorrentistaService;
import br.edu.ifpb.pweb2.intelliwallet.service.LoginJaCadastradoException;
import br.edu.ifpb.pweb2.intelliwallet.service.VisaoService;
import jakarta.servlet.http.HttpSession;
import jakarta.validation.Valid;

@Controller
public class CorrentistaController {

    private final CorrentistaService correntistaService;
    private final VisaoService visaoService;

    public CorrentistaController(CorrentistaService correntistaService, VisaoService visaoService) {
        this.correntistaService = correntistaService;
        this.visaoService = visaoService;
    }

    @ModelAttribute
    public void verificarVisao(HttpSession sessao) {
        visaoService.exigirAdministrador(sessao);
    }

    @GetMapping({"/correntistas", "/admin/correntistas"})
    public String listar(Model model) {
        model.addAttribute("correntistas", correntistaService.listarCorrentistas());
        return "correntistas/lista";
    }

    @GetMapping("/correntistas/novo")
    public String novo(Model model) {
        model.addAttribute("formulario", new CorrentistaForm());
        return "correntistas/formulario";
    }

    @PostMapping("/correntistas/novo")
    public String criar(@Valid @ModelAttribute("formulario") CorrentistaForm formulario,
                        BindingResult resultado,
                        RedirectAttributes atributos) {
        if (formulario.getSenha() == null || formulario.getSenha().trim().length() < 8) {
            resultado.rejectValue("senha", "senha.invalida", "A senha deve ter pelo menos 8 caracteres");
        }

        if (resultado.hasErrors()) {
            return "correntistas/formulario";
        }

        try {
            correntistaService.criar(formulario);
        } catch (LoginJaCadastradoException ex) {
            resultado.rejectValue("login", "login.duplicado", ex.getMessage());
            return "correntistas/formulario";
        }

        atributos.addFlashAttribute("mensagem", "Correntista cadastrado com sucesso.");
        return "redirect:/correntistas";
    }

    @GetMapping({"/correntistas/{id}/editar", "/correntistas/editar/{id}"})
    public String editar(@PathVariable Long id, Model model) {
        Correntista correntista = correntistaService.buscarPorId(id);
        model.addAttribute("formulario", new CorrentistaForm(correntista));
        return "correntistas/formulario";
    }

    @PostMapping({"/correntistas/{id}/editar", "/correntistas/editar/{id}"})
    public String atualizar(@PathVariable Long id,
                            @Valid @ModelAttribute("formulario") CorrentistaForm formulario,
                            BindingResult resultado,
                            RedirectAttributes atributos) {
        if (formulario.getSenha() != null && !formulario.getSenha().isBlank()
                && formulario.getSenha().trim().length() < 8) {
            resultado.rejectValue("senha", "senha.invalida", "A senha deve ter pelo menos 8 caracteres");
        }

        if (resultado.hasErrors()) {
            formulario.setId(id);
            return "correntistas/formulario";
        }

        try {
            correntistaService.atualizar(id, formulario);
        } catch (LoginJaCadastradoException ex) {
            formulario.setId(id);
            resultado.rejectValue("login", "login.duplicado", ex.getMessage());
            return "correntistas/formulario";
        }

        atributos.addFlashAttribute("mensagem", "Correntista atualizado com sucesso.");
        return "redirect:/correntistas";
    }

    @PostMapping({"/correntistas/{id}/excluir", "/correntistas/excluir/{id}"})
    public String excluir(@PathVariable Long id, RedirectAttributes atributos) {
        correntistaService.excluir(id);
        atributos.addFlashAttribute("mensagem", "Correntista excluído com sucesso.");
        return "redirect:/correntistas";
    }
}

