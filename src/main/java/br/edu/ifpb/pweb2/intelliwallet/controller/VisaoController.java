package br.edu.ifpb.pweb2.intelliwallet.controller;

import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.GetMapping;

@Controller
public class VisaoController {

    @GetMapping("/admin")
    public String areaAdministrador() {
        return "redirect:/correntistas";
    }
}
