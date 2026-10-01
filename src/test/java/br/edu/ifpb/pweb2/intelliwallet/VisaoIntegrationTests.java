package br.edu.ifpb.pweb2.intelliwallet;

import static org.assertj.core.api.Assertions.assertThat;
import static org.hamcrest.Matchers.containsString;
import static org.hamcrest.Matchers.not;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.content;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.flash;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.model;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.redirectedUrl;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.webmvc.test.autoconfigure.AutoConfigureMockMvc;
import org.springframework.mock.web.MockHttpSession;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.transaction.annotation.Transactional;

import br.edu.ifpb.pweb2.intelliwallet.model.Conta;
import br.edu.ifpb.pweb2.intelliwallet.model.Correntista;
import br.edu.ifpb.pweb2.intelliwallet.model.Papel;
import br.edu.ifpb.pweb2.intelliwallet.repository.ContaRepository;
import br.edu.ifpb.pweb2.intelliwallet.repository.CorrentistaRepository;
import br.edu.ifpb.pweb2.intelliwallet.repository.TransacaoRepository;

@SpringBootTest
@AutoConfigureMockMvc
@Transactional
class VisaoIntegrationTests {

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private CorrentistaRepository correntistaRepository;

    @Autowired
    private ContaRepository contaRepository;

    @Autowired
    private TransacaoRepository transacaoRepository;

    private Long correntistaId() {
        return correntistaRepository.findByLoginIgnoreCase("teste").orElseThrow().getId();
    }

    private MockHttpSession entrarComoCorrentista(Long id) throws Exception {
        MockHttpSession sessao = new MockHttpSession();
        mockMvc.perform(get("/correntista/{id}", id).session(sessao))
                .andExpect(status().is3xxRedirection())
                .andExpect(redirectedUrl("/contas"));
        return sessao;
    }

    private Correntista outroCorrentista() {
        return correntistaRepository.saveAndFlush(
                new Correntista("Outro correntista", "outro_visao_test", "hash-teste", Papel.CORRENTISTA));
    }

    @Test
    void inicioNaoAssumeVisaoAdministrativaEContasPedemSelecao() throws Exception {
        mockMvc.perform(get("/"))
                .andExpect(status().isOk())
                .andExpect(content().string(not(containsString("Visão de administrador"))));
        mockMvc.perform(get("/contas"))
                .andExpect(redirectedUrl("/correntista"));
        mockMvc.perform(get("/correntistas"))
                .andExpect(status().isForbidden());
        mockMvc.perform(get("/cadastro"))
                .andExpect(status().isOk());
    }

    @Test
    void entrarComoCorrentistaMantemVisaoNaNavegacaoEIgnoraDonoNaQuery() throws Exception {
        Long id = correntistaId();
        MockHttpSession sessao = entrarComoCorrentista(id);
        Long outroId = outroCorrentista().getId();
        mockMvc.perform(get("/contas").session(sessao).param("correntistaId", outroId.toString()))
                .andExpect(status().isOk())
                .andExpect(model().attribute("correntista", correntistaRepository.findById(id).orElseThrow()))
                .andExpect(content().string(containsString("Nova conta")))
                .andExpect(content().string(not(containsString("href=\"/correntistas\""))));
        mockMvc.perform(get("/contas/nova").session(sessao))
                .andExpect(status().isOk());
        mockMvc.perform(get("/correntistas/novo").session(sessao))
                .andExpect(status().isForbidden());
        mockMvc.perform(post("/correntistas/novo").session(sessao))
                .andExpect(status().isForbidden());
    }

    @Test
    void administradorECorrentistaNaoFicamSelecionadosAoMesmoTempo() throws Exception {
        MockHttpSession sessao = entrarComoCorrentista(correntistaId());
        mockMvc.perform(get("/admin").session(sessao))
                .andExpect(redirectedUrl("/correntistas"));
        assertThat(sessao.getAttribute("correntistaId")).isNull();
        assertThat(sessao.getAttribute("administrador")).isEqualTo(true);
        mockMvc.perform(get("/admin/correntistas").session(sessao))
                .andExpect(status().isOk())
                .andExpect(content().string(containsString("Visão de administrador")))
                .andExpect(content().string(not(containsString("href=\"/contas\""))));
        mockMvc.perform(post("/contas").session(sessao))
                .andExpect(status().isForbidden());
        mockMvc.perform(get("/correntista/{id}", correntistaId()).session(sessao));
        assertThat(sessao.getAttribute("administrador")).isNull();
        assertThat(sessao.getAttribute("correntistaId")).isEqualTo(correntistaId());
        mockMvc.perform(post("/sair").session(sessao))
                .andExpect(redirectedUrl("/"));
        assertThat(sessao.isInvalid()).isTrue();
    }

    @Test
    void naoSelecionaAdministradorComoCorrentistaNemIdInexistente() throws Exception {
        Long adminId = correntistaRepository.findByLoginIgnoreCase("admin").orElseThrow().getId();
        mockMvc.perform(get("/correntista/{id}", adminId)).andExpect(status().isNotFound());
        mockMvc.perform(get("/correntista/{id}", Long.MAX_VALUE)).andExpect(status().isNotFound());
    }

    @Test
    void outroCorrentistaNaoAcessaNemAlteraContasTransacoesEComentarios() throws Exception {
        Conta conta = contaRepository.findByCorrentistaIdAndNumero(correntistaId(), "1001-0").orElseThrow();
        Long transacaoId = transacaoRepository.findByContaIdOrderByDataDescIdDesc(conta.getId()).get(0).getId();
        MockHttpSession sessao = entrarComoCorrentista(outroCorrentista().getId());
        long quantidadeTransacoes = transacaoRepository.count();
        mockMvc.perform(get("/contas/{id}", conta.getId()).session(sessao)).andExpect(status().isNotFound());
        mockMvc.perform(get("/contas/{id}/transacoes/nova", conta.getId()).session(sessao))
                .andExpect(status().isNotFound());
        mockMvc.perform(post("/contas/{id}/transacoes", conta.getId()).session(sessao))
                .andExpect(status().isNotFound());
        mockMvc.perform(post("/contas/{id}/transacoes/{transacaoId}", conta.getId(), transacaoId).session(sessao))
                .andExpect(status().isNotFound());
        mockMvc.perform(get("/transacoes/{id}/comentario/novo", transacaoId).session(sessao))
                .andExpect(status().isNotFound());
        mockMvc.perform(post("/transacoes/{id}/comentario", transacaoId).session(sessao).param("texto", "Outro dono"))
                .andExpect(status().isNotFound());
        assertThat(transacaoRepository.count()).isEqualTo(quantidadeTransacoes);
    }

    @Test
    void cadastraContaCorrenteParaDonoDaSessaoComPrg() throws Exception {
        Long id = correntistaId();
        MockHttpSession sessao = entrarComoCorrentista(id);
        Long outroId = outroCorrentista().getId();
        mockMvc.perform(post("/contas").session(sessao)
                        .param("numero", " 9090-1 ").param("descricao", " Minha conta ")
                        .param("tipo", "CORRENTE").param("diaFechamento", "15")
                        .param("correntistaId", outroId.toString()))
                .andExpect(status().is3xxRedirection())
                .andExpect(redirectedUrl("/contas"))
                .andExpect(flash().attributeExists("mensagem"));
        Conta conta = contaRepository.findByCorrentistaIdAndNumero(id, "9090-1").orElseThrow();
        assertThat(conta.getCorrentista().getId()).isEqualTo(id);
        assertThat(conta.getDescricao()).isEqualTo("Minha conta");
        assertThat(conta.getDiaFechamento()).isNull();
        mockMvc.perform(get("/contas").session(sessao))
                .andExpect(content().string(containsString("Minha conta")));
        assertThat(contaRepository.findByCorrentistaIdAndNumero(outroId, "9090-1")).isEmpty();
    }

    @Test
    void cadastraCartaoComFechamentoESemDuplicarNumeroDoMesmoDono() throws Exception {
        MockHttpSession sessao = entrarComoCorrentista(correntistaId());
        mockMvc.perform(post("/contas").session(sessao)
                        .param("numero", "9090-2").param("descricao", "Meu cartão")
                        .param("tipo", "CARTAO").param("diaFechamento", "31"))
                .andExpect(redirectedUrl("/contas"));
        assertThat(contaRepository.findByCorrentistaIdAndNumero(correntistaId(), "9090-2")
                .orElseThrow().getDiaFechamento()).isEqualTo(31);
        long quantidade = contaRepository.count();
        mockMvc.perform(post("/contas").session(sessao)
                        .param("numero", "9090-2").param("descricao", "Duplicado").param("tipo", "CORRENTE"))
                .andExpect(redirectedUrl("/contas/nova"))
                .andExpect(flash().attributeExists("org.springframework.validation.BindingResult.formulario"));
        mockMvc.perform(get("/contas/nova").session(sessao))
                .andExpect(model().attributeHasFieldErrors("formulario", "numero"))
                .andExpect(content().string(containsString("Você já possui uma conta com este número")));
        assertThat(contaRepository.count()).isEqualTo(quantidade);
    }

    @Test
    void validacaoDeCartaoEDeCamposFazPrgSemGravarDados() throws Exception {
        MockHttpSession sessao = entrarComoCorrentista(correntistaId());
        long quantidade = contaRepository.count();
        mockMvc.perform(post("/contas").session(sessao)
                        .param("numero", "9090-3").param("descricao", "Sem fechamento").param("tipo", "CARTAO"))
                .andExpect(redirectedUrl("/contas/nova"));
        mockMvc.perform(get("/contas/nova").session(sessao))
                .andExpect(model().attributeHasFieldErrors("formulario", "diaFechamento"))
                .andExpect(content().string(containsString("Informe o dia de fechamento do cartão")));
        mockMvc.perform(post("/contas").session(sessao)
                        .param("numero", " ").param("descricao", " ")
                        .param("tipo", "CARTAO").param("diaFechamento", "32"))
                .andExpect(redirectedUrl("/contas/nova"));
        mockMvc.perform(get("/contas/nova").session(sessao))
                .andExpect(model().attributeHasFieldErrors("formulario", "numero", "descricao", "diaFechamento"));
        assertThat(contaRepository.count()).isEqualTo(quantidade);
    }

    @Test
    void donosDiferentesPodemUsarMesmoNumeroDeConta() throws Exception {
        Long outroId = outroCorrentista().getId();
        MockHttpSession sessao = entrarComoCorrentista(outroId);
        mockMvc.perform(post("/contas").session(sessao)
                        .param("numero", "1001-0").param("descricao", "Outro banco").param("tipo", "CORRENTE"))
                .andExpect(redirectedUrl("/contas"));
        assertThat(contaRepository.findByCorrentistaIdAndNumero(outroId, "1001-0")).isPresent();
        assertThat(contaRepository.findByCorrentistaIdAndNumero(correntistaId(), "1001-0")).isPresent();
    }
}
