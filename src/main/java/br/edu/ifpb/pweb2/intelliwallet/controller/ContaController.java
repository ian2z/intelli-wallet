package br.edu.ifpb.pweb2.intelliwallet.controller;

import java.time.LocalDate;
import java.time.YearMonth;
import java.time.format.DateTimeParseException;
import java.util.List;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.format.annotation.DateTimeFormat;
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

import br.edu.ifpb.pweb2.intelliwallet.model.Conta;
import br.edu.ifpb.pweb2.intelliwallet.model.ContaForm;
import br.edu.ifpb.pweb2.intelliwallet.model.Correntista;
import br.edu.ifpb.pweb2.intelliwallet.model.ExtratoConta;
import br.edu.ifpb.pweb2.intelliwallet.model.TipoConta;
import br.edu.ifpb.pweb2.intelliwallet.model.Transacao;
import br.edu.ifpb.pweb2.intelliwallet.service.ContaInvalidaException;
import br.edu.ifpb.pweb2.intelliwallet.service.ContaService;
import br.edu.ifpb.pweb2.intelliwallet.service.OrcamentoService;
import br.edu.ifpb.pweb2.intelliwallet.service.TransacaoService;
import br.edu.ifpb.pweb2.intelliwallet.service.VisaoService;
import jakarta.validation.Valid;

@Controller
@RequestMapping("/contas")
public class ContaController {

    private static final int TAMANHO_PAGINA_CONTAS = 10;
    private static final int TAMANHO_PAGINA_EXTRATO = 10;
    private static final List<String> NOMES_MESES = List.of(
            "Janeiro", "Fevereiro", "Março", "Abril", "Maio", "Junho",
            "Julho", "Agosto", "Setembro", "Outubro", "Novembro", "Dezembro"
    );

    private final ContaService contaService;
    private final VisaoService visaoService;
    private final TransacaoService transacaoService;
    private final OrcamentoService orcamentoService;

    public ContaController(ContaService contaService,
                           VisaoService visaoService,
                           TransacaoService transacaoService,
                           OrcamentoService orcamentoService) {
        this.contaService = contaService;
        this.visaoService = visaoService;
        this.transacaoService = transacaoService;
        this.orcamentoService = orcamentoService;
    }

    @GetMapping
    public String listar(@RequestParam(defaultValue = "0") int pagina, Model model) {
        Correntista correntista = visaoService.exigirCorrentista();
        int paginaSegura = Math.max(0, pagina);
        Page<Conta> page = contaService.listarPorCorrentista(
                correntista.getId(), PageRequest.of(paginaSegura, TAMANHO_PAGINA_CONTAS));
        List<Conta> contas = page.getContent();
        model.addAttribute("correntista", correntista);
        model.addAttribute("pagina", page);
        model.addAttribute("contas", contas);
        model.addAttribute("saldos", transacaoService.calcularSaldosPorContas(contas));
        return "contas/lista";
    }

    @GetMapping("/nova")
    public String nova(Model model) {
        visaoService.exigirCorrentista();
        if (!model.containsAttribute("formulario")) {
            model.addAttribute("formulario", new ContaForm());
        }
        model.addAttribute("tipos", TipoConta.values());
        return "contas/formulario";
    }

    @PostMapping
    public String criar(@Valid @ModelAttribute("formulario") ContaForm formulario,
                        BindingResult resultado,
                        RedirectAttributes atributos) {
        Correntista correntista = visaoService.exigirCorrentista();
        if (!resultado.hasErrors()) {
            try {
                contaService.criar(correntista, formulario);
            } catch (ContaInvalidaException ex) {
                resultado.rejectValue(ex.getCampo(), "conta.invalida", ex.getMessage());
            }
        }
        if (resultado.hasErrors()) {
            atributos.addFlashAttribute("formulario", formulario);
            atributos.addFlashAttribute(BindingResult.MODEL_KEY_PREFIX + "formulario", resultado);
            return "redirect:/contas/nova";
        }
        atributos.addFlashAttribute("mensagem", "Conta cadastrada com sucesso.");
        return "redirect:/contas";
    }

    @PostMapping("/{id}/excluir")
    public String excluir(@PathVariable Long id, RedirectAttributes atributos) {
        visaoService.exigirAdministrador();
        contaService.excluir(id);
        atributos.addFlashAttribute("mensagem", "Conta e suas transações excluídas com sucesso.");
        return "redirect:/contas";
    }

    // UC07: Extrato da conta por mês corrente ou período arbitrário, com paginação no banco (RNF 07)
    @GetMapping({"/{id}", "/{id}/transacoes", "/{id}/extrato"})
    public String extrato(@PathVariable Long id,
                          @RequestParam(required = false) String mes,
                          @RequestParam(required = false) Integer mesNumero,
                          @RequestParam(required = false) Integer ano,
                          @RequestParam(required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate dataInicio,
                          @RequestParam(required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate dataFim,
                          @RequestParam(defaultValue = "0") int pagina,
                          Model model) {
        visaoService.verificarConta(id);

        LocalDate inicio;
        LocalDate fim;

        if (dataInicio != null && dataFim != null) {
            if (dataFim.isBefore(dataInicio)) {
                inicio = dataFim;
                fim = dataInicio;
            } else {
                inicio = dataInicio;
                fim = dataFim;
            }
        } else if (mesNumero != null && mesNumero >= 1 && mesNumero <= 12) {
            int anoEscolhido = (ano != null && ano >= 1900 && ano <= 2200)
                    ? ano : YearMonth.now().getYear();
            YearMonth ym = YearMonth.of(anoEscolhido, mesNumero);
            inicio = ym.atDay(1);
            fim = ym.atEndOfMonth();
        } else if (mes != null && !mes.isBlank()) {
            YearMonth ym = parseYearMonthOuAtual(mes);
            inicio = ym.atDay(1);
            fim = ym.atEndOfMonth();
        } else {
            YearMonth atual = YearMonth.now();
            inicio = atual.atDay(1);
            fim = atual.atEndOfMonth();
        }

        int paginaSegura = Math.max(0, pagina);
        ExtratoConta extrato = transacaoService.gerarExtrato(
                id, inicio, fim, PageRequest.of(paginaSegura, TAMANHO_PAGINA_EXTRATO));

        List<Transacao> todasTransacoes = transacaoService.listarPorConta(id);
        Long donoId = extrato.conta().getCorrentista().getId();
        List<Integer> anosDisponiveis = orcamentoService.listarAnosDisponiveis(donoId, inicio.getYear());

        model.addAttribute("conta", extrato.conta());
        model.addAttribute("extrato", extrato);
        model.addAttribute("itens", extrato.itens());
        model.addAttribute("transacoes", extrato.pagina().getContent());
        model.addAttribute("pagina", extrato.pagina());
        model.addAttribute("resumo", transacaoService.calcularResumo(todasTransacoes));
        model.addAttribute("dataInicio", inicio);
        model.addAttribute("dataFim", fim);
        model.addAttribute("mesNumeroSelecionado", inicio.getMonthValue());
        model.addAttribute("anoSelecionado", inicio.getYear());
        model.addAttribute("anosDisponiveis", anosDisponiveis);
        model.addAttribute("meses", NOMES_MESES);
        return "contas/transacoes";
    }

    private YearMonth parseYearMonthOuAtual(String mes) {
        try {
            return YearMonth.parse(mes.trim());
        } catch (DateTimeParseException ex) {
            return YearMonth.now();
        }
    }
}
