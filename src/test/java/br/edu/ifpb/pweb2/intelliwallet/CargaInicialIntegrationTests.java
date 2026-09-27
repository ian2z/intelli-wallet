package br.edu.ifpb.pweb2.intelliwallet;

import static org.assertj.core.api.Assertions.assertThat;

import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.core.io.ClassPathResource;
import org.springframework.jdbc.datasource.init.ResourceDatabasePopulator;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.transaction.annotation.Transactional;

import javax.sql.DataSource;

import br.edu.ifpb.pweb2.intelliwallet.model.Conta;
import br.edu.ifpb.pweb2.intelliwallet.repository.CategoriaRepository;
import br.edu.ifpb.pweb2.intelliwallet.repository.ContaRepository;
import br.edu.ifpb.pweb2.intelliwallet.repository.CorrentistaRepository;
import br.edu.ifpb.pweb2.intelliwallet.repository.TransacaoRepository;

@SpringBootTest
@Transactional
class CargaInicialIntegrationTests {

    @Autowired
    private DataSource dataSource;

    @Autowired
    private CategoriaRepository categoriaRepository;

    @Autowired
    private CorrentistaRepository correntistaRepository;

    @Autowired
    private ContaRepository contaRepository;

    @Autowired
    private TransacaoRepository transacaoRepository;

    @Test
    void cargaInicialNaoDuplicaDadosAoExecutarNovamente() {
        long correntistas = correntistaRepository.count();
        long contas = contaRepository.count();
        long transacoes = transacaoRepository.count();

        new ResourceDatabasePopulator(new ClassPathResource("data.sql"),
                new ClassPathResource("data-dev.sql")).execute(dataSource);

        assertThat(categoriaRepository.count()).isEqualTo(22);
        assertThat(correntistaRepository.count()).isEqualTo(correntistas);
        assertThat(contaRepository.count()).isEqualTo(contas);
        assertThat(transacaoRepository.count()).isEqualTo(transacoes);
        assertThat(new BCryptPasswordEncoder().matches("admin_dev_123",
                correntistaRepository.findByLoginIgnoreCase("admin").orElseThrow().getSenha())).isTrue();
    }

    @Test
    void excluirContaExcluiSuasTransacoes() {
        Long correntistaId = correntistaRepository.findByLoginIgnoreCase("teste").orElseThrow().getId();
        Conta conta = contaRepository.findByCorrentistaIdAndNumero(correntistaId, "1001-0").orElseThrow();
        Long contaId = conta.getId();
        assertThat(transacaoRepository.countByContaId(contaId)).isPositive();

        contaRepository.delete(conta);
        contaRepository.flush();

        assertThat(transacaoRepository.countByContaId(contaId)).isZero();
    }
}
