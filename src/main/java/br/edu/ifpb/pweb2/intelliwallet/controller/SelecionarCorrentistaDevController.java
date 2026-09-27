package br.edu.ifpb.pweb2.intelliwallet.controller;

import org.springframework.context.annotation.Profile;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;

import br.edu.ifpb.pweb2.intelliwallet.model.Papel;
import br.edu.ifpb.pweb2.intelliwallet.repository.CorrentistaRepository;
import br.edu.ifpb.pweb2.intelliwallet.service.CorrentistaAtualService;
import jakarta.servlet.http.HttpSession;

@Controller
@Profile("dev")
public class SelecionarCorrentistaDevController {

    private final CorrentistaRepository correntistaRepository;

    public SelecionarCorrentistaDevController(CorrentistaRepository correntistaRepository) {
        this.correntistaRepository = correntistaRepository;
    }

    @GetMapping("/dev/correntistas")
    public String formulario(Model model) {
        model.addAttribute("correntistas",
                correntistaRepository.findByPapelAndBloqueadoFalseOrderByNomeAsc(Papel.CORRENTISTA));
        return "dev/selecionar-correntista";
    }

    @PostMapping("/dev/correntistas/selecionar")
    public String selecionar(@RequestParam Long correntistaId, HttpSession sessao,
            RedirectAttributes atributos) {
        boolean disponivel = correntistaRepository.findById(correntistaId)
                .filter(correntista -> correntista.getPapel() == Papel.CORRENTISTA)
                .filter(correntista -> !correntista.isBloqueado())
                .isPresent();
        if (!disponivel) {
            atributos.addFlashAttribute("erro", "Selecione um correntista disponível.");
            return "redirect:/dev/correntistas";
        }
        sessao.setAttribute(CorrentistaAtualService.CHAVE_SESSAO, correntistaId);
        return "redirect:/contas";
    }
}
