package br.edu.ifpb.pweb2.intelliwallet;

import static org.assertj.core.api.Assertions.assertThat;
import static org.hamcrest.Matchers.containsString;
import static org.hamcrest.Matchers.not;
import static org.springframework.security.test.web.servlet.request.SecurityMockMvcRequestPostProcessors.csrf;
import static org.springframework.security.test.web.servlet.request.SecurityMockMvcRequestPostProcessors.user;
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

    private Correntista outroCorrentista() {
        return correntistaRepository.saveAndFlush(
                new Correntista("Outro correntista", "outro_visao_test", "hash-teste", Papel.CORRENTISTA));
    }

    @Test
    void rotasProtegidasRedirecionamParaLoginSemAutenticacao() throws Exception {
        mockMvc.perform(get("/"))
                .andExpect(status().isOk());
        mockMvc.perform(get("/login"))
                .andExpect(status().isOk());
        mockMvc.perform(get("/cadastro"))
                .andExpect(status().isOk());
        mockMvc.perform(get("/contas"))
                .andExpect(status().is3xxRedirection())
                .andExpect(redirectedUrl("/login"));
        mockMvc.perform(get("/correntistas"))
                .andExpect(status().is3xxRedirection())
                .andExpect(redirectedUrl("/login"));
        mockMvc.perform(get("/categorias"))
                .andExpect(status().is3xxRedirection())
                .andExpect(redirectedUrl("/login"));
    }

    @Test
    void correntistaNaoAcessaRotasAdministrativas() throws Exception {
        mockMvc.perform(get("/contas").with(user("teste").roles("CORRENTISTA")))
                .andExpect(status().isOk())
                .andExpect(content().string(containsString("Nova conta")))
                .andExpect(content().string(not(containsString("href=\"/correntistas\""))));
        mockMvc.perform(get("/correntistas").with(user("teste").roles("CORRENTISTA")))
                .andExpect(status().isForbidden());
        mockMvc.perform(get("/categorias").with(user("teste").roles("CORRENTISTA")))
                .andExpect(status().isForbidden());
        mockMvc.perform(post("/correntistas/novo").with(user("teste").roles("CORRENTISTA")).with(csrf()))
                .andExpect(status().isForbidden());
    }

    @Test
    void administradorAcessaRotasAdministrativasEPropriasContas() throws Exception {
        mockMvc.perform(get("/correntistas").with(user("admin").roles("ADMIN")))
                .andExpect(status().isOk())
                .andExpect(content().string(containsString("Correntistas")));
        mockMvc.perform(get("/categorias").with(user("admin").roles("ADMIN")))
                .andExpect(status().isOk())
                .andExpect(content().string(containsString("Categorias")));
        mockMvc.perform(get("/contas").with(user("admin").roles("ADMIN")))
                .andExpect(status().isOk());
    }

    @Test
    void outroCorrentistaNaoAcessaNemAlteraContasTransacoesEComentarios() throws Exception {
        Conta conta = contaRepository.findByCorrentistaIdAndNumero(correntistaId(), "1001-0").orElseThrow();
        Long transacaoId = transacaoRepository.findByContaIdOrderByDataDescIdDesc(conta.getId()).get(0).getId();
        outroCorrentista();
        long quantidadeTransacoes = transacaoRepository.count();

        mockMvc.perform(get("/contas/{id}", conta.getId()).with(user("outro_visao_test").roles("CORRENTISTA")))
                .andExpect(status().isNotFound());
        mockMvc.perform(get("/contas/{id}/transacoes/nova", conta.getId()).with(user("outro_visao_test").roles("CORRENTISTA")))
                .andExpect(status().isNotFound());
        mockMvc.perform(post("/contas/{id}/transacoes", conta.getId()).with(user("outro_visao_test").roles("CORRENTISTA")).with(csrf()))
                .andExpect(status().isNotFound());
        mockMvc.perform(post("/contas/{id}/transacoes/{transacaoId}", conta.getId(), transacaoId)
                        .with(user("outro_visao_test").roles("CORRENTISTA")).with(csrf()))
                .andExpect(status().isNotFound());
        mockMvc.perform(get("/transacoes/{id}/comentario/novo", transacaoId).with(user("outro_visao_test").roles("CORRENTISTA")))
                .andExpect(status().isNotFound());
        mockMvc.perform(post("/transacoes/{id}/comentario", transacaoId)
                        .with(user("outro_visao_test").roles("CORRENTISTA")).with(csrf())
                        .param("texto", "Outro dono"))
                .andExpect(status().isNotFound());
        assertThat(transacaoRepository.count()).isEqualTo(quantidadeTransacoes);
    }

    @Test
    void cadastraContaCorrenteParaUsuarioAutenticadoComPrg() throws Exception {
        Long id = correntistaId();
        mockMvc.perform(post("/contas").with(user("teste").roles("CORRENTISTA")).with(csrf())
                        .param("numero", " 9090-1 ").param("descricao", " Minha conta ")
                        .param("tipo", "CORRENTE").param("diaFechamento", "15"))
                .andExpect(status().is3xxRedirection())
                .andExpect(redirectedUrl("/contas"))
                .andExpect(flash().attributeExists("mensagem"));
        Conta conta = contaRepository.findByCorrentistaIdAndNumero(id, "9090-1").orElseThrow();
        assertThat(conta.getCorrentista().getId()).isEqualTo(id);
        assertThat(conta.getDescricao()).isEqualTo("Minha conta");
        assertThat(conta.getDiaFechamento()).isNull();
    }

    @Test
    void validacaoDeCartaoEDeCamposFazPrgSemGravarDados() throws Exception {
        long quantidade = contaRepository.count();
        mockMvc.perform(post("/contas").with(user("teste").roles("CORRENTISTA")).with(csrf())
                        .param("numero", "9090-3").param("descricao", "Sem fechamento").param("tipo", "CARTAO"))
                .andExpect(redirectedUrl("/contas/nova"));
        mockMvc.perform(post("/contas").with(user("teste").roles("CORRENTISTA")).with(csrf())
                        .param("numero", " ").param("descricao", " ")
                        .param("tipo", "CARTAO").param("diaFechamento", "32"))
                .andExpect(redirectedUrl("/contas/nova"));
        assertThat(contaRepository.count()).isEqualTo(quantidade);
    }
}
