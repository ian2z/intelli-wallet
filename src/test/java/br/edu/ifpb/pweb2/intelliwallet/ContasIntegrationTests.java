package br.edu.ifpb.pweb2.intelliwallet;

import static org.assertj.core.api.Assertions.assertThat;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.model;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.redirectedUrl;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

import java.util.List;

import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.webmvc.test.autoconfigure.AutoConfigureMockMvc;
import org.springframework.mock.web.MockHttpSession;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.transaction.annotation.Transactional;

import br.edu.ifpb.pweb2.intelliwallet.model.Conta;
import br.edu.ifpb.pweb2.intelliwallet.model.Correntista;
import br.edu.ifpb.pweb2.intelliwallet.model.TipoConta;
import br.edu.ifpb.pweb2.intelliwallet.repository.ContaRepository;
import br.edu.ifpb.pweb2.intelliwallet.repository.CorrentistaRepository;
import br.edu.ifpb.pweb2.intelliwallet.service.CorrentistaAtualService;

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
    void visitanteEEncaminhadoAoSeletorDeDesenvolvimento() throws Exception {
        mockMvc.perform(get("/contas"))
                .andExpect(status().is3xxRedirection())
                .andExpect(redirectedUrl("/dev/correntistas"));
    }

    @Test
    void seletorExibeSomenteContasDoCorrentistaEscolhido() throws Exception {
        Long id = correntistaRepository.findByLoginIgnoreCase("teste").orElseThrow().getId();
        MockHttpSession sessao = new MockHttpSession();

        mockMvc.perform(post("/dev/correntistas/selecionar")
                        .param("correntistaId", id.toString())
                        .session(sessao))
                .andExpect(status().is3xxRedirection())
                .andExpect(redirectedUrl("/contas"));

        assertThat(sessao.getAttribute(CorrentistaAtualService.CHAVE_SESSAO)).isEqualTo(id);
        mockMvc.perform(get("/contas").session(sessao))
                .andExpect(status().isOk())
                .andExpect(model().attributeExists("contas"))
                .andExpect(result -> {
                    @SuppressWarnings("unchecked")
                    List<Conta> contas = (List<Conta>) result.getModelAndView().getModel().get("contas");
                    assertThat(contas).hasSize(2)
                            .allMatch(conta -> conta.getCorrentista().getId().equals(id));
                });
    }

    @Test
    void naoPermiteVerTransacoesDeOutraPessoa() throws Exception {
        Correntista outro = new Correntista();
        outro.setNome("Outro correntista");
        outro.setLogin("outro_teste_uc02");
        outro.setSenha("hash-de-teste");
        outro = correntistaRepository.saveAndFlush(outro);

        Conta contaDeOutro = new Conta();
        contaDeOutro.setCorrentista(outro);
        contaDeOutro.setNumero("9000-1");
        contaDeOutro.setDescricao("Conta reservada");
        contaDeOutro.setTipo(TipoConta.CORRENTE);
        contaDeOutro = contaRepository.saveAndFlush(contaDeOutro);

        Long correntistaId = correntistaRepository.findByLoginIgnoreCase("teste").orElseThrow().getId();
        MockHttpSession sessao = new MockHttpSession();
        sessao.setAttribute(CorrentistaAtualService.CHAVE_SESSAO, correntistaId);

        mockMvc.perform(get("/contas/{id}/transacoes", contaDeOutro.getId()).session(sessao))
                .andExpect(status().isNotFound());
    }
}
