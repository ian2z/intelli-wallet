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
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.transaction.annotation.Transactional;

import br.edu.ifpb.pweb2.intelliwallet.model.Papel;
import br.edu.ifpb.pweb2.intelliwallet.repository.CorrentistaRepository;

@SpringBootTest
@AutoConfigureMockMvc
@Transactional
class CadastroCorrentistaIntegrationTests {

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private CorrentistaRepository correntistaRepository;

    @Test
    void formularioPublicoEAbre() throws Exception {
        mockMvc.perform(get("/cadastro"))
                .andExpect(status().isOk())
                .andExpect(view().name("correntistas/cadastro"));
    }

    @Test
    void cadastroValidoRedirecionaEArmazenaHash() throws Exception {
        mockMvc.perform(post("/cadastro")
                        .param("nome", "Maria Silva")
                        .param("login", "MARIA")
                        .param("senha", "senhaSegura123"))
                .andExpect(status().is3xxRedirection())
                .andExpect(redirectedUrl("/"))
                .andExpect(flash().attributeExists("mensagem"));

        var correntista = correntistaRepository.findByLoginIgnoreCase("maria").orElseThrow();
        assertThat(correntista.getPapel()).isEqualTo(Papel.CORRENTISTA);
        assertThat(new BCryptPasswordEncoder().matches("senhaSegura123", correntista.getSenha())).isTrue();
    }

    @Test
    void loginDuplicadoMostraErroNoCampo() throws Exception {
        mockMvc.perform(post("/cadastro")
                        .param("nome", "Outro usuário")
                        .param("login", "TESTE")
                        .param("senha", "senhaSegura123"))
                .andExpect(status().isOk())
                .andExpect(view().name("correntistas/cadastro"))
                .andExpect(model().attributeHasFieldErrors("formulario", "login"));
    }
}
