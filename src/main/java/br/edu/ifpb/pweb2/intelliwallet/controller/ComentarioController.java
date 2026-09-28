package br.edu.ifpb.pweb2.intelliwallet.controller;

import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.validation.BindingResult;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.ModelAttribute;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.server.ResponseStatusException;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;

import br.edu.ifpb.pweb2.intelliwallet.model.Comentario;
import br.edu.ifpb.pweb2.intelliwallet.model.ComentarioForm;
import br.edu.ifpb.pweb2.intelliwallet.model.Transacao;
import br.edu.ifpb.pweb2.intelliwallet.repository.TransacaoRepository;
import br.edu.ifpb.pweb2.intelliwallet.service.ComentarioJaExisteException;
import br.edu.ifpb.pweb2.intelliwallet.service.ComentarioService;
import jakarta.validation.Valid;

@Controller
@RequestMapping("/transacoes/{transacaoId}/comentario")
public class ComentarioController {

    private final ComentarioService comentarioService;
    private final TransacaoRepository transacaoRepository;

    public ComentarioController(ComentarioService comentarioService, TransacaoRepository transacaoRepository) {
        this.comentarioService = comentarioService;
        this.transacaoRepository = transacaoRepository;
    }

    @GetMapping("/novo")
    public String formularioNovo(@PathVariable Long transacaoId,
                                  @RequestParam(required = false) String voltarPara,
                                  Model model) {
        buscarTransacaoOu404(transacaoId);
        if (comentarioService.buscarPorTransacaoId(transacaoId).isPresent()) {
            return "redirect:/transacoes/" + transacaoId + "/comentario/editar";
        }
        ComentarioForm formulario = new ComentarioForm();
        formulario.setVoltarPara(voltarPara);
        model.addAttribute("formulario", formulario);
        model.addAttribute("transacaoId", transacaoId);
        model.addAttribute("acao", "criar");
        return "comentarios/formulario";
    }

    @PostMapping
    public String criar(@PathVariable Long transacaoId,
                         @Valid @ModelAttribute("formulario") ComentarioForm formulario,
                         BindingResult resultado,
                         Model model,
                         RedirectAttributes atributos) {
        Transacao transacao = buscarTransacaoOu404(transacaoId);
        if (resultado.hasErrors()) {
            model.addAttribute("transacaoId", transacaoId);
            model.addAttribute("acao", "criar");
            return "comentarios/formulario";
        }
        if (comentarioService.buscarPorTransacaoId(transacaoId).isPresent()) {
            atributos.addFlashAttribute("erro", "Esta transação já possui um comentário");
            return "redirect:/transacoes/" + transacaoId + "/comentario/editar";
        }
        try {
            comentarioService.criar(transacao, formulario);
        } catch (ComentarioJaExisteException ex) {
            atributos.addFlashAttribute("erro", ex.getMessage());
            return "redirect:/transacoes/" + transacaoId + "/comentario/editar";
        }
        atributos.addFlashAttribute("mensagem", "Comentário adicionado com sucesso");
        return "redirect:" + destinoSeguro(formulario.getVoltarPara());
    }

    @GetMapping("/editar")
    public String formularioEditar(@PathVariable Long transacaoId,
                                    @RequestParam(required = false) String voltarPara,
                                    Model model) {
        buscarTransacaoOu404(transacaoId);
        Comentario comentario = comentarioService.buscarPorTransacaoId(transacaoId)
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND));
        ComentarioForm formulario = new ComentarioForm();
        formulario.setTexto(comentario.getTexto());
        formulario.setVoltarPara(voltarPara);
        model.addAttribute("formulario", formulario);
        model.addAttribute("transacaoId", transacaoId);
        model.addAttribute("acao", "editar");
        return "comentarios/formulario";
    }

    @PostMapping("/editar")
    public String atualizar(@PathVariable Long transacaoId,
                             @Valid @ModelAttribute("formulario") ComentarioForm formulario,
                             BindingResult resultado,
                             Model model,
                             RedirectAttributes atributos) {
        buscarTransacaoOu404(transacaoId);
        Comentario comentario = comentarioService.buscarPorTransacaoId(transacaoId)
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND));
        if (resultado.hasErrors()) {
            model.addAttribute("transacaoId", transacaoId);
            model.addAttribute("acao", "editar");
            return "comentarios/formulario";
        }
        comentarioService.atualizar(comentario, formulario.getTexto());
        atributos.addFlashAttribute("mensagem", "Comentário atualizado com sucesso");
        return "redirect:" + destinoSeguro(formulario.getVoltarPara());
    }

    @PostMapping("/excluir")
    public String excluir(@PathVariable Long transacaoId,
                           @RequestParam(required = false) String voltarPara,
                           RedirectAttributes atributos) {
        buscarTransacaoOu404(transacaoId);
        Comentario comentario = comentarioService.buscarPorTransacaoId(transacaoId)
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND));
        comentarioService.excluir(comentario);
        atributos.addFlashAttribute("mensagem", "Comentário excluído com sucesso");
        return "redirect:" + destinoSeguro(voltarPara);
    }

    private Transacao buscarTransacaoOu404(Long transacaoId) {
        return transacaoRepository.findById(transacaoId)
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND));
    }

    private String destinoSeguro(String voltarPara) {
        if (voltarPara != null && voltarPara.startsWith("/")) {
            return voltarPara;
        }
        return "/";
    }
}
