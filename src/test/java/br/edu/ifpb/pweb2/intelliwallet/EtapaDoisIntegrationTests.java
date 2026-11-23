package br.edu.ifpb.pweb2.intelliwallet;

import static org.assertj.core.api.Assertions.assertThat;
import static org.hamcrest.Matchers.containsString;
import static org.springframework.security.test.web.servlet.request.SecurityMockMvcRequestBuilders.formLogin;
import static org.springframework.security.test.web.servlet.request.SecurityMockMvcRequestPostProcessors.csrf;
import static org.springframework.security.test.web.servlet.request.SecurityMockMvcRequestPostProcessors.user;
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

import br.edu.ifpb.pweb2.intelliwallet.model.Categoria;
import br.edu.ifpb.pweb2.intelliwallet.model.Correntista;
import br.edu.ifpb.pweb2.intelliwallet.repository.CategoriaRepository;
import br.edu.ifpb.pweb2.intelliwallet.repository.CorrentistaRepository;

@SpringBootTest
@AutoConfigureMockMvc
@Transactional
class EtapaDoisIntegrationTests {

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private CorrentistaRepository correntistaRepository;

    @Autowired
    private CategoriaRepository categoriaRepository;

    @Test
    void autenticacaoValidaRedirecionaPorPapelEInvalidaFalha() throws Exception {
        mockMvc.perform(formLogin("/login").user("login", "admin").password("senha", "admin_dev_123"))
                .andExpect(status().is3xxRedirection())
                .andExpect(redirectedUrl("/correntistas"));

        mockMvc.perform(formLogin("/login").user("login", "teste").password("senha", "teste_dev_123"))
                .andExpect(status().is3xxRedirection())
                .andExpect(redirectedUrl("/contas"));

        mockMvc.perform(formLogin("/login").user("login", "teste").password("senha", "senha_errada"))
                .andExpect(status().is3xxRedirection())
                .andExpect(redirectedUrl("/login?erro"));
    }

    @Test
    void uc24AdminBloqueiaCorrentistaEBloqueadoNaoConsegueLogar() throws Exception {
        Correntista teste = correntistaRepository.findByLoginIgnoreCase("teste").orElseThrow();
        assertThat(teste.isBloqueado()).isFalse();

        mockMvc.perform(post("/correntistas/{id}/bloquear", teste.getId())
                        .with(user("admin").roles("ADMIN")).with(csrf()))
                .andExpect(status().is3xxRedirection())
                .andExpect(redirectedUrl("/correntistas?pagina=0"));

        assertThat(correntistaRepository.findById(teste.getId()).orElseThrow().isBloqueado()).isTrue();

        mockMvc.perform(formLogin("/login").user("login", "teste").password("senha", "teste_dev_123"))
                .andExpect(status().is3xxRedirection())
                .andExpect(redirectedUrl("/login?bloqueado"));
    }

    @Test
    void uc22e23AdminListaCriaEditaEDesativaCategoriaComPaginacao() throws Exception {
        mockMvc.perform(get("/categorias").with(user("admin").roles("ADMIN")))
                .andExpect(status().isOk())
                .andExpect(view().name("categorias/lista"))
                .andExpect(model().attributeExists("pagina", "categorias"));

        mockMvc.perform(post("/categorias").with(user("admin").roles("ADMIN")).with(csrf())
                        .param("nome", "Assinaturas Digitais")
                        .param("natureza", "SAIDA")
                        .param("ordem", "15")
                        .param("ativo", "true"))
                .andExpect(status().is3xxRedirection())
                .andExpect(redirectedUrl("/categorias"));

        Categoria criada = categoriaRepository.findByNome("Assinaturas Digitais").orElseThrow();
        assertThat(criada.isAtivo()).isTrue();

        mockMvc.perform(post("/categorias/{id}/alternar-status", criada.getId())
                        .with(user("admin").roles("ADMIN")).with(csrf()))
                .andExpect(status().is3xxRedirection());

        assertThat(categoriaRepository.findById(criada.getId()).orElseThrow().isAtivo()).isFalse();
    }

    @Test
    void uc08e09CorrentistaAcessaOrcamentoAnualEGrafico() throws Exception {
        mockMvc.perform(get("/orcamento").param("ano", "2026").with(user("teste").roles("CORRENTISTA")))
                .andExpect(status().isOk())
                .andExpect(view().name("orcamento/anual"))
                .andExpect(model().attributeExists("orcamento", "anosDisponiveis", "meses"))
                .andExpect(content().string(containsString("Entradas")))
                .andExpect(content().string(containsString("Saídas")))
                .andExpect(content().string(containsString("Investimentos")));

        mockMvc.perform(get("/grafico").param("ano", "2026").with(user("teste").roles("CORRENTISTA")))
                .andExpect(status().isOk())
                .andExpect(view().name("orcamento/grafico"))
                .andExpect(model().attributeExists("orcamento"));
    }
}
