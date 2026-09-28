package br.edu.ifpb.pweb2.intelliwallet.controller;

import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;

import br.edu.ifpb.pweb2.intelliwallet.service.CorrentistaService;

@Controller
public class CorrentistaController {

    private final CorrentistaService correntistaService;

    public CorrentistaController(CorrentistaService correntistaService) {
        this.correntistaService = correntistaService;
    }

    @GetMapping({"/correntistas", "/admin/correntistas"})
    public String listar(Model model) {
        model.addAttribute("correntistas", correntistaService.listarCorrentistas());
        return "correntistas/lista";
    }
}
