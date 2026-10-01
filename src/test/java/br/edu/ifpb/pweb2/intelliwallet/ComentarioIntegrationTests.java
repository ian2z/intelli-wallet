package br.edu.ifpb.pweb2.intelliwallet;

import static org.assertj.core.api.Assertions.assertThat;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.content;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.model;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.redirectedUrl;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.view;

import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.webmvc.test.autoconfigure.AutoConfigureMockMvc;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.transaction.annotation.Transactional;

import br.edu.ifpb.pweb2.intelliwallet.model.Conta;
import br.edu.ifpb.pweb2.intelliwallet.repository.ComentarioRepository;
import br.edu.ifpb.pweb2.intelliwallet.repository.ContaRepository;
import br.edu.ifpb.pweb2.intelliwallet.repository.CorrentistaRepository;
import br.edu.ifpb.pweb2.intelliwallet.repository.TransacaoRepository;

@SpringBootTest
@AutoConfigureMockMvc
@Transactional
class ComentarioIntegrationTests {

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private CorrentistaRepository correntistaRepository;

    @Autowired
    private ContaRepository contaRepository;

    @Autowired
    private TransacaoRepository transacaoRepository;

    @Autowired
    private ComentarioRepository comentarioRepository;

    private Long correntistaId() {
        return correntistaRepository.findByLoginIgnoreCase("teste").orElseThrow().getId();
    }

    private Long transacaoSeedId() {
        Long correntistaId = correntistaRepository.findByLoginIgnoreCase("teste").orElseThrow().getId();
        Conta conta = contaRepository.findByCorrentistaIdAndNumero(correntistaId, "1001-0").orElseThrow();
        return transacaoRepository.findByContaIdOrderByDataDescIdDesc(conta.getId()).get(0).getId();
    }

    @Test
    void mostraFormularioDeCriacaoQuandoNaoHaComentario() throws Exception {
        Long transacaoId = transacaoSeedId();

        mockMvc.perform(get("/transacoes/{id}/comentario/novo", transacaoId).sessionAttr("correntistaId", correntistaId()))
                .andExpect(status().isOk())
                .andExpect(view().name("comentarios/formulario"))
                .andExpect(model().attribute("acao", "criar"));
    }

    @Test
    void criaComentarioComTextoValido() throws Exception {
        Long transacaoId = transacaoSeedId();

        mockMvc.perform(post("/transacoes/{id}/comentario", transacaoId).sessionAttr("correntistaId", correntistaId())
                        .param("texto", "Compra parcelada em 3x")
                        .param("voltarPara", "/contas"))
                .andExpect(status().is3xxRedirection())
                .andExpect(redirectedUrl("/contas"));

        assertThat(comentarioRepository.findByTransacaoId(transacaoId)).isPresent();
        assertThat(comentarioRepository.findByTransacaoId(transacaoId).get().getTexto())
                .isEqualTo("Compra parcelada em 3x");
    }

    @Test
    void naoCriaComentarioComTextoEmBranco() throws Exception {
        Long transacaoId = transacaoSeedId();

        mockMvc.perform(post("/transacoes/{id}/comentario", transacaoId).sessionAttr("correntistaId", correntistaId())
                        .param("texto", "")
                        .param("voltarPara", "/contas"))
                .andExpect(status().isOk())
                .andExpect(view().name("comentarios/formulario"))
                .andExpect(model().attributeHasFieldErrors("formulario", "texto"));

        assertThat(comentarioRepository.findByTransacaoId(transacaoId)).isEmpty();
    }

    @Test
    void redirecionaParaEdicaoQuandoJaExisteComentario() throws Exception {
        Long transacaoId = transacaoSeedId();
        mockMvc.perform(post("/transacoes/{id}/comentario", transacaoId).sessionAttr("correntistaId", correntistaId())
                .param("texto", "Primeiro comentário")
                .param("voltarPara", "/contas"));

        mockMvc.perform(get("/transacoes/{id}/comentario/novo", transacaoId).sessionAttr("correntistaId", correntistaId()))
                .andExpect(status().is3xxRedirection())
                .andExpect(redirectedUrl("/transacoes/" + transacaoId + "/comentario/editar"));
    }

    @Test
    void atualizaTextoDoComentarioExistente() throws Exception {
        Long transacaoId = transacaoSeedId();
        mockMvc.perform(post("/transacoes/{id}/comentario", transacaoId).sessionAttr("correntistaId", correntistaId())
                .param("texto", "Texto original")
                .param("voltarPara", "/contas"));

        mockMvc.perform(post("/transacoes/{id}/comentario/editar", transacaoId).sessionAttr("correntistaId", correntistaId())
                        .param("texto", "Texto corrigido")
                        .param("voltarPara", "/contas"))
                .andExpect(status().is3xxRedirection())
                .andExpect(redirectedUrl("/contas"));

        assertThat(comentarioRepository.findByTransacaoId(transacaoId).get().getTexto())
                .isEqualTo("Texto corrigido");
    }

    @Test
    void excluiComentarioExistente() throws Exception {
        Long transacaoId = transacaoSeedId();
        mockMvc.perform(post("/transacoes/{id}/comentario", transacaoId).sessionAttr("correntistaId", correntistaId())
                .param("texto", "Comentário a ser removido")
                .param("voltarPara", "/contas"));

        mockMvc.perform(post("/transacoes/{id}/comentario/excluir", transacaoId).sessionAttr("correntistaId", correntistaId())
                        .param("voltarPara", "/contas"))
                .andExpect(status().is3xxRedirection())
                .andExpect(redirectedUrl("/contas"));

        assertThat(comentarioRepository.findByTransacaoId(transacaoId)).isEmpty();
    }

    @Test
    void rejeitaVoltarParaProtocolRelativoAoExcluir() throws Exception {
        Long transacaoId = transacaoSeedId();
        mockMvc.perform(post("/transacoes/{id}/comentario", transacaoId).sessionAttr("correntistaId", correntistaId())
                .param("texto", "Comentário a ser removido")
                .param("voltarPara", "/contas"));

        mockMvc.perform(post("/transacoes/{id}/comentario/excluir", transacaoId).sessionAttr("correntistaId", correntistaId())
                        .param("voltarPara", "//evil.com"))
                .andExpect(status().is3xxRedirection())
                .andExpect(redirectedUrl("/"));
    }

    @Test
    void retorna404ParaTransacaoInexistente() throws Exception {
        mockMvc.perform(get("/transacoes/{id}/comentario/novo", 999999L).sessionAttr("correntistaId", correntistaId()))
                .andExpect(status().isNotFound());
    }

    @Test
    void retorna404AoEditarTransacaoSemComentario() throws Exception {
        Long transacaoId = transacaoSeedId();

        mockMvc.perform(get("/transacoes/{id}/comentario/editar", transacaoId).sessionAttr("correntistaId", correntistaId()))
                .andExpect(status().isNotFound());
    }

    @Test
    void mostraFormularioDeEdicaoComTextoExistente() throws Exception {
        Long transacaoId = transacaoSeedId();
        mockMvc.perform(post("/transacoes/{id}/comentario", transacaoId).sessionAttr("correntistaId", correntistaId())
                .param("texto", "Texto original")
                .param("voltarPara", "/contas"));

        mockMvc.perform(get("/transacoes/{id}/comentario/editar", transacaoId).sessionAttr("correntistaId", correntistaId()))
                .andExpect(status().isOk())
                .andExpect(view().name("comentarios/formulario"))
                .andExpect(model().attribute("acao", "editar"))
                .andExpect(content().string(org.hamcrest.Matchers.containsString("Texto original")));
    }
}
