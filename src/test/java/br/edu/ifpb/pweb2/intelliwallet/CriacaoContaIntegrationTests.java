package br.edu.ifpb.pweb2.intelliwallet;

import static org.assertj.core.api.Assertions.assertThat;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.flash;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.model;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.redirectedUrl;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.view;

import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.webmvc.test.autoconfigure.AutoConfigureMockMvc;
import org.springframework.mock.web.MockHttpSession;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.transaction.annotation.Transactional;

import br.edu.ifpb.pweb2.intelliwallet.model.TipoConta;
import br.edu.ifpb.pweb2.intelliwallet.repository.ContaRepository;
import br.edu.ifpb.pweb2.intelliwallet.repository.CorrentistaRepository;
import br.edu.ifpb.pweb2.intelliwallet.service.CorrentistaAtualService;

@SpringBootTest
@AutoConfigureMockMvc
@Transactional
class CriacaoContaIntegrationTests {

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private CorrentistaRepository correntistaRepository;

    @Autowired
    private ContaRepository contaRepository;

    @Test
    void formularioExigeCorrentistaSelecionado() throws Exception {
        mockMvc.perform(get("/contas/nova"))
                .andExpect(status().is3xxRedirection())
                .andExpect(redirectedUrl("/dev/correntistas"));
    }

    @Test
    void criaContaCorrenteComPostRedirectGet() throws Exception {
        Long correntistaId = idDoCorrentistaDeTeste();
        mockMvc.perform(post("/contas")
                        .session(sessaoDo(correntistaId))
                        .param("numero", "2002-1")
                        .param("descricao", "Conta de uso diário")
                        .param("tipo", "CORRENTE")
                        .param("diaFechamento", "15"))
                .andExpect(status().is3xxRedirection())
                .andExpect(redirectedUrl("/contas"))
                .andExpect(flash().attributeExists("mensagem"));

        var conta = contaRepository.findByCorrentistaIdAndNumero(correntistaId, "2002-1").orElseThrow();
        assertThat(conta.getTipo()).isEqualTo(TipoConta.CORRENTE);
        assertThat(conta.getDiaFechamento()).isNull();
    }

    @Test
    void criaCartaoComDiaDeFechamento() throws Exception {
        Long correntistaId = idDoCorrentistaDeTeste();
        mockMvc.perform(post("/contas")
                        .session(sessaoDo(correntistaId))
                        .param("numero", "3003-2")
                        .param("descricao", "Cartão de compras")
                        .param("tipo", "CARTAO")
                        .param("diaFechamento", "12"))
                .andExpect(status().is3xxRedirection())
                .andExpect(redirectedUrl("/contas"));

        var conta = contaRepository.findByCorrentistaIdAndNumero(correntistaId, "3003-2").orElseThrow();
        assertThat(conta.getTipo()).isEqualTo(TipoConta.CARTAO);
        assertThat(conta.getDiaFechamento()).isEqualTo(12);
    }

    @Test
    void cartaoExigeDiaDeFechamentoValido() throws Exception {
        Long correntistaId = idDoCorrentistaDeTeste();
        mockMvc.perform(post("/contas")
                        .session(sessaoDo(correntistaId))
                        .param("numero", "5000-1")
                        .param("descricao", "Cartão de teste")
                        .param("tipo", "CARTAO")
                        .param("diaFechamento", "32"))
                .andExpect(status().isOk())
                .andExpect(view().name("contas/formulario"))
                .andExpect(model().attributeHasFieldErrors("formulario", "diaFechamento"));
        assertThat(contaRepository.findByCorrentistaIdAndNumero(correntistaId, "5000-1")).isEmpty();
    }

    @Test
    void numeroDuplicadoMostraErroNoCampo() throws Exception {
        mockMvc.perform(post("/contas")
                        .session(sessaoDo(idDoCorrentistaDeTeste()))
                        .param("numero", "1001-0")
                        .param("descricao", "Conta repetida")
                        .param("tipo", "CORRENTE"))
                .andExpect(status().isOk())
                .andExpect(view().name("contas/formulario"))
                .andExpect(model().attributeHasFieldErrors("formulario", "numero"));
    }

    private Long idDoCorrentistaDeTeste() {
        return correntistaRepository.findByLoginIgnoreCase("teste").orElseThrow().getId();
    }

    private MockHttpSession sessaoDo(Long correntistaId) {
        MockHttpSession sessao = new MockHttpSession();
        sessao.setAttribute(CorrentistaAtualService.CHAVE_SESSAO, correntistaId);
        return sessao;
    }
}
