package br.edu.ifpb.pweb2.intelliwallet;

import static org.assertj.core.api.Assertions.assertThat;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.model;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.view;

import java.math.BigDecimal;
import java.util.List;
import java.util.Map;

import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.webmvc.test.autoconfigure.AutoConfigureMockMvc;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.transaction.annotation.Transactional;

import br.edu.ifpb.pweb2.intelliwallet.model.Conta;
import br.edu.ifpb.pweb2.intelliwallet.model.Correntista;
import br.edu.ifpb.pweb2.intelliwallet.model.ResumoSaldo;
import br.edu.ifpb.pweb2.intelliwallet.model.TipoConta;
import br.edu.ifpb.pweb2.intelliwallet.repository.ContaRepository;
import br.edu.ifpb.pweb2.intelliwallet.repository.CorrentistaRepository;

@SpringBootTest
@AutoConfigureMockMvc
@Transactional
class ContasIntegrationTests {

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private CorrentistaRepository correntistaRepository;

    @Autowired
    private ContaRepository contaRepository;

    @Test
    void mostraEscolhaDeCorrentistaAntesDaListagem() throws Exception {
        mockMvc.perform(get("/contas"))
                .andExpect(status().isOk())
                .andExpect(view().name("contas/lista"))
                .andExpect(model().attributeExists("correntistas"));
    }

    @Test
    void listaApenasContasDoCorrentistaSelecionado() throws Exception {
        Long id = correntistaRepository.findByLoginIgnoreCase("teste").orElseThrow().getId();

        mockMvc.perform(get("/contas").param("correntistaId", id.toString()))
                .andExpect(status().isOk())
                .andExpect(result -> {
                    @SuppressWarnings("unchecked")
                    List<Conta> contas = (List<Conta>) result.getModelAndView().getModel().get("contas");
                    assertThat(contas).hasSize(2)
                            .allMatch(conta -> conta.getCorrentista().getId().equals(id));
                });
    }

    @Test
    void naoMisturaContasDeCorrentistasDiferentes() throws Exception {
        Correntista outro = new Correntista();
        outro.setNome("Outro correntista");
        outro.setLogin("outro_teste_uc02");
        outro.setSenha("hash-de-teste");
        outro = correntistaRepository.saveAndFlush(outro);

        Conta contaDeOutro = new Conta();
        contaDeOutro.setCorrentista(outro);
        contaDeOutro.setNumero("9000-1");
        contaDeOutro.setDescricao("Conta de outra pessoa");
        contaDeOutro.setTipo(TipoConta.CORRENTE);
        contaDeOutro = contaRepository.saveAndFlush(contaDeOutro);

        Long outroId = outro.getId();
        Long contaId = contaDeOutro.getId();
        mockMvc.perform(get("/contas").param("correntistaId", outroId.toString()))
                .andExpect(status().isOk())
                .andExpect(result -> {
                    @SuppressWarnings("unchecked")
                    List<Conta> contas = (List<Conta>) result.getModelAndView().getModel().get("contas");
                    assertThat(contas).extracting(Conta::getId).containsExactly(contaId);
                });
    }

    @Test
    void administradorNaoEntraNaListagemDeCorrentistas() throws Exception {
        Long adminId = correntistaRepository.findByLoginIgnoreCase("admin").orElseThrow().getId();

        mockMvc.perform(get("/contas").param("correntistaId", adminId.toString()))
                .andExpect(status().isNotFound());
    }

    @Test
    void incluiSaldosDasContasAoListarPorCorrentista() throws Exception {
        Long id = correntistaRepository.findByLoginIgnoreCase("teste").orElseThrow().getId();

        mockMvc.perform(get("/contas").param("correntistaId", id.toString()))
                .andExpect(status().isOk())
                .andExpect(model().attributeExists("saldos"))
                .andExpect(result -> {
                    @SuppressWarnings("unchecked")
                    Map<Long, BigDecimal> saldos = (Map<Long, BigDecimal>) result.getModelAndView().getModel().get("saldos");
                    assertThat(saldos).isNotEmpty();
                });
    }

    @Test
    void exibeResumoComSaldoNaVisualizacaoDeTransacoesDaConta() throws Exception {
        Conta conta = contaRepository.findAll().stream().findFirst().orElseThrow();

        mockMvc.perform(get("/contas/{id}", conta.getId()))
                .andExpect(status().isOk())
                .andExpect(view().name("contas/transacoes"))
                .andExpect(model().attributeExists("resumo"))
                .andExpect(result -> {
                    ResumoSaldo resumo = (ResumoSaldo) result.getModelAndView().getModel().get("resumo");
                    assertThat(resumo).isNotNull();
                    assertThat(resumo.saldo()).isNotNull();
                });
    }
}
