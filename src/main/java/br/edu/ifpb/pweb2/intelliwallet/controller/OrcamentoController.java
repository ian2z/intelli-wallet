package br.edu.ifpb.pweb2.intelliwallet.controller;

import java.time.Year;
import java.util.List;

import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestParam;

import br.edu.ifpb.pweb2.intelliwallet.model.Conta;
import br.edu.ifpb.pweb2.intelliwallet.model.Correntista;
import br.edu.ifpb.pweb2.intelliwallet.model.OrcamentoAnual;
import br.edu.ifpb.pweb2.intelliwallet.service.ContaService;
import br.edu.ifpb.pweb2.intelliwallet.service.OrcamentoService;
import br.edu.ifpb.pweb2.intelliwallet.service.VisaoService;

@Controller
public class OrcamentoController {

    private static final List<String> NOMES_MESES = List.of(
            "Janeiro", "Fevereiro", "Março", "Abril", "Maio", "Junho",
            "Julho", "Agosto", "Setembro", "Outubro", "Novembro", "Dezembro"
    );

    private final OrcamentoService orcamentoService;
    private final ContaService contaService;
    private final VisaoService visaoService;

    public OrcamentoController(OrcamentoService orcamentoService,
                               ContaService contaService,
                               VisaoService visaoService) {
        this.orcamentoService = orcamentoService;
        this.contaService = contaService;
        this.visaoService = visaoService;
    }

    @GetMapping("/orcamento")
    public String orcamentoAnual(@RequestParam(required = false) Integer ano,
                                 @RequestParam(required = false) Long contaId,
                                 Model model) {
        prepararModelo(ano, contaId, model);
        return "orcamento/anual";
    }

    @GetMapping("/grafico")
    public String graficoAnual(@RequestParam(required = false) Integer ano,
                               @RequestParam(required = false) Long contaId,
                               Model model) {
        prepararModelo(ano, contaId, model);
        return "orcamento/grafico";
    }

    private void prepararModelo(Integer anoParam, Long contaId, Model model) {
        Correntista usuario = visaoService.exigirCorrentista();
        int ano = (anoParam != null && anoParam >= 1900 && anoParam <= 2200)
                ? anoParam
                : Year.now().getValue();

        if (contaId != null) {
            visaoService.verificarConta(contaId);
        }

        List<Conta> contas = contaService.listarPorCorrentista(usuario.getId());
        OrcamentoAnual orcamento = orcamentoService.gerarOrcamento(usuario.getId(), ano, contaId);
        List<Integer> anosDisponiveis = orcamentoService.listarAnosDisponiveis(usuario.getId(), ano);

        model.addAttribute("orcamento", orcamento);
        model.addAttribute("anoSelecionado", ano);
        model.addAttribute("anosDisponiveis", anosDisponiveis);
        model.addAttribute("contas", contas);
        model.addAttribute("contaIdSelecionada", contaId);
        model.addAttribute("meses", NOMES_MESES);
    }
}
