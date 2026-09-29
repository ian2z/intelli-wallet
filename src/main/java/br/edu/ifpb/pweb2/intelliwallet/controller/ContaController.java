package br.edu.ifpb.pweb2.intelliwallet.controller;

import java.util.List;

import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.server.ResponseStatusException;

import br.edu.ifpb.pweb2.intelliwallet.model.Conta;
import br.edu.ifpb.pweb2.intelliwallet.model.Correntista;
import br.edu.ifpb.pweb2.intelliwallet.model.Papel;
import br.edu.ifpb.pweb2.intelliwallet.repository.ContaRepository;
import br.edu.ifpb.pweb2.intelliwallet.repository.CorrentistaRepository;
import br.edu.ifpb.pweb2.intelliwallet.service.TransacaoService;
import org.springframework.web.bind.annotation.PathVariable;

@Controller
@RequestMapping("/contas")
public class ContaController {

    private final ContaRepository contaRepository;
    private final CorrentistaRepository correntistaRepository;
    private final TransacaoService transacaoService;

    public ContaController(ContaRepository contaRepository,
                           CorrentistaRepository correntistaRepository,
                           TransacaoService transacaoService) {
        this.contaRepository = contaRepository;
        this.correntistaRepository = correntistaRepository;
        this.transacaoService = transacaoService;
    }

    @GetMapping
    public String listar(@RequestParam(required = false) Long correntistaId, Model model) {
        model.addAttribute("correntistas", correntistaRepository.findByPapelOrderByNomeAsc(Papel.CORRENTISTA));
        model.addAttribute("contas", List.of());

        if (correntistaId != null) {
            Correntista correntista = correntistaRepository.findById(correntistaId)
                    .filter(pessoa -> pessoa.getPapel() == Papel.CORRENTISTA)
                    .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND));
            model.addAttribute("correntista", correntista);
            model.addAttribute("contas", contaRepository.findByCorrentistaIdOrderByIdAsc(correntistaId));
        }

        return "contas/lista";
    }

    @GetMapping({"/{id}", "/{id}/transacoes"})
    public String transacoes(@PathVariable Long id, Model model) {
        Conta conta = transacaoService.buscarConta(id);
        model.addAttribute("conta", conta);
        model.addAttribute("transacoes", transacaoService.listarPorConta(id));
        return "contas/transacoes";
    }
}
