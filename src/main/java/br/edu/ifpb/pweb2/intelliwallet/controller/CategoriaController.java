package br.edu.ifpb.pweb2.intelliwallet.controller;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.validation.BindingResult;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.ModelAttribute;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;

import br.edu.ifpb.pweb2.intelliwallet.model.Categoria;
import br.edu.ifpb.pweb2.intelliwallet.model.CategoriaForm;
import br.edu.ifpb.pweb2.intelliwallet.model.Natureza;
import br.edu.ifpb.pweb2.intelliwallet.service.CategoriaInvalidaException;
import br.edu.ifpb.pweb2.intelliwallet.service.CategoriaService;
import br.edu.ifpb.pweb2.intelliwallet.service.VisaoService;
import jakarta.validation.Valid;

@Controller
@RequestMapping("/categorias")
public class CategoriaController {

    private static final int TAMANHO_PAGINA = 10;

    private final CategoriaService categoriaService;
    private final VisaoService visaoService;

    public CategoriaController(CategoriaService categoriaService, VisaoService visaoService) {
        this.categoriaService = categoriaService;
        this.visaoService = visaoService;
    }

    @ModelAttribute
    public void verificarVisao() {
        visaoService.exigirAdministrador();
    }

    @GetMapping
    public String listar(@RequestParam(defaultValue = "0") int pagina, Model model) {
        int paginaSegura = Math.max(0, pagina);
        Page<Categoria> page = categoriaService.listarPaginado(PageRequest.of(paginaSegura, TAMANHO_PAGINA));
        model.addAttribute("pagina", page);
        model.addAttribute("categorias", page.getContent());
        return "categorias/lista";
    }

    @GetMapping("/nova")
    public String nova(Model model) {
        if (!model.containsAttribute("formulario")) {
            model.addAttribute("formulario", new CategoriaForm());
        }
        model.addAttribute("naturezas", Natureza.values());
        return "categorias/formulario";
    }

    @PostMapping
    public String criar(@Valid @ModelAttribute("formulario") CategoriaForm formulario,
                        BindingResult resultado,
                        Model model,
                        RedirectAttributes atributos) {
        if (resultado.hasErrors()) {
            model.addAttribute("naturezas", Natureza.values());
            return "categorias/formulario";
        }
        try {
            categoriaService.criar(formulario);
        } catch (CategoriaInvalidaException ex) {
            resultado.rejectValue(ex.getCampo(), "categoria.invalida", ex.getMessage());
            model.addAttribute("naturezas", Natureza.values());
            return "categorias/formulario";
        }
        atributos.addFlashAttribute("mensagem", "Categoria cadastrada com sucesso.");
        return "redirect:/categorias";
    }

    @GetMapping("/{id}/editar")
    public String editar(@PathVariable Long id, Model model) {
        Categoria categoria = categoriaService.buscarPorId(id);
        model.addAttribute("formulario", new CategoriaForm(categoria));
        model.addAttribute("naturezas", Natureza.values());
        return "categorias/formulario";
    }

    @PostMapping("/{id}/editar")
    public String atualizar(@PathVariable Long id,
                            @Valid @ModelAttribute("formulario") CategoriaForm formulario,
                            BindingResult resultado,
                            Model model,
                            RedirectAttributes atributos) {
        if (resultado.hasErrors()) {
            formulario.setId(id);
            model.addAttribute("naturezas", Natureza.values());
            return "categorias/formulario";
        }
        try {
            categoriaService.atualizar(id, formulario);
        } catch (CategoriaInvalidaException ex) {
            formulario.setId(id);
            resultado.rejectValue(ex.getCampo(), "categoria.invalida", ex.getMessage());
            model.addAttribute("naturezas", Natureza.values());
            return "categorias/formulario";
        }
        atributos.addFlashAttribute("mensagem", "Categoria atualizada com sucesso.");
        return "redirect:/categorias";
    }

    @PostMapping("/{id}/alternar-status")
    public String alternarStatus(@PathVariable Long id,
                                 @RequestParam(defaultValue = "0") int pagina,
                                 RedirectAttributes atributos) {
        Categoria categoria = categoriaService.alternarAtivo(id);
        String msg = categoria.isAtivo()
                ? "Categoria \"" + categoria.getNome() + "\" reativada com sucesso."
                : "Categoria \"" + categoria.getNome() + "\" desativada com sucesso.";
        atributos.addFlashAttribute("mensagem", msg);
        return "redirect:/categorias?pagina=" + Math.max(0, pagina);
    }
}
