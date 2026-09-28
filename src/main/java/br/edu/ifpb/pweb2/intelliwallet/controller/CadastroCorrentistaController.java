package br.edu.ifpb.pweb2.intelliwallet.controller;

import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.validation.BindingResult;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.ModelAttribute;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;

import br.edu.ifpb.pweb2.intelliwallet.model.CadastroCorrentistaForm;
import br.edu.ifpb.pweb2.intelliwallet.service.CorrentistaService;
import br.edu.ifpb.pweb2.intelliwallet.service.LoginJaCadastradoException;
import jakarta.validation.Valid;

@Controller
public class CadastroCorrentistaController {

    private final CorrentistaService correntistaService;

    public CadastroCorrentistaController(CorrentistaService correntistaService) {
        this.correntistaService = correntistaService;
    }

    @GetMapping("/cadastro")
    public String formulario(Model model) {
        model.addAttribute("formulario", new CadastroCorrentistaForm());
        return "correntistas/cadastro";
    }

    @PostMapping("/cadastro")
    public String cadastrar(@Valid @ModelAttribute("formulario") CadastroCorrentistaForm formulario,
            BindingResult resultado, RedirectAttributes atributos) {
        if (resultado.hasErrors()) {
            return "correntistas/cadastro";
        }
        try {
            correntistaService.cadastrar(formulario);
        } catch (LoginJaCadastradoException ex) {
            resultado.rejectValue("login", "login.duplicado", ex.getMessage());
            return "correntistas/cadastro";
        }
        atributos.addFlashAttribute("mensagem", "Cadastro concluído. Sua conta de acesso está pronta.");
        return "redirect:/";
    }
}
