package br.edu.ifpb.pweb2.intelliwallet;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.when;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.List;
import java.util.Map;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import br.edu.ifpb.pweb2.intelliwallet.model.Conta;
import br.edu.ifpb.pweb2.intelliwallet.model.Movimento;
import br.edu.ifpb.pweb2.intelliwallet.model.ResumoSaldo;
import br.edu.ifpb.pweb2.intelliwallet.model.Transacao;
import br.edu.ifpb.pweb2.intelliwallet.repository.CategoriaRepository;
import br.edu.ifpb.pweb2.intelliwallet.repository.ContaRepository;
import br.edu.ifpb.pweb2.intelliwallet.repository.TransacaoRepository;
import br.edu.ifpb.pweb2.intelliwallet.service.TransacaoService;

@ExtendWith(MockitoExtension.class)
class SaldoServiceUnitTests {

    @Mock
    private TransacaoRepository transacaoRepository;

    @Mock
    private ContaRepository contaRepository;

    @Mock
    private CategoriaRepository categoriaRepository;

    private TransacaoService transacaoService;

    @BeforeEach
    void setUp() {
        transacaoService = new TransacaoService(transacaoRepository, contaRepository, categoriaRepository);
    }

    @Test
    void retornaResumoZeradoQuandoNaoHaTransacoes() {
        ResumoSaldo resumo = transacaoService.calcularResumo(List.of());

        assertThat(resumo.saldo()).isEqualByComparingTo(BigDecimal.ZERO);
        assertThat(resumo.totalCreditos()).isEqualByComparingTo(BigDecimal.ZERO);
        assertThat(resumo.totalDebitos()).isEqualByComparingTo(BigDecimal.ZERO);
        assertThat(resumo.isPositivo()).isTrue();
        assertThat(resumo.isNegativo()).isFalse();
    }

    @Test
    void calculaSaldoPositivoComCreditosEDebitos() {
        Transacao t1 = new Transacao();
        t1.setData(LocalDate.now());
        t1.setDescricao("Salário");
        t1.setValor(new BigDecimal("3500.00"));
        t1.setMovimento(Movimento.CREDITO);

        Transacao t2 = new Transacao();
        t2.setData(LocalDate.now());
        t2.setDescricao("Feira");
        t2.setValor(new BigDecimal("500.00"));
        t2.setMovimento(Movimento.DEBITO);

        Transacao t3 = new Transacao();
        t3.setData(LocalDate.now());
        t3.setDescricao("Cashback");
        t3.setValor(new BigDecimal("50.00"));
        t3.setMovimento(Movimento.CREDITO);

        ResumoSaldo resumo = transacaoService.calcularResumo(List.of(t1, t2, t3));

        assertThat(resumo.totalCreditos()).isEqualByComparingTo(new BigDecimal("3550.00"));
        assertThat(resumo.totalDebitos()).isEqualByComparingTo(new BigDecimal("500.00"));
        assertThat(resumo.saldo()).isEqualByComparingTo(new BigDecimal("3050.00"));
        assertThat(resumo.isPositivo()).isTrue();
        assertThat(resumo.isNegativo()).isFalse();
    }

    @Test
    void calculaSaldoNegativoQuandoDebitosSuperamCreditos() {
        Transacao t1 = new Transacao();
        t1.setValor(new BigDecimal("100.00"));
        t1.setMovimento(Movimento.CREDITO);

        Transacao t2 = new Transacao();
        t2.setValor(new BigDecimal("250.00"));
        t2.setMovimento(Movimento.DEBITO);

        ResumoSaldo resumo = transacaoService.calcularResumo(List.of(t1, t2));

        assertThat(resumo.totalCreditos()).isEqualByComparingTo(new BigDecimal("100.00"));
        assertThat(resumo.totalDebitos()).isEqualByComparingTo(new BigDecimal("250.00"));
        assertThat(resumo.saldo()).isEqualByComparingTo(new BigDecimal("-150.00"));
        assertThat(resumo.isNegativo()).isTrue();
        assertThat(resumo.isPositivo()).isFalse();
    }

    @Test
    void calculaSaldoPorContaBuscandoNoRepositorio() {
        Long contaId = 10L;

        Transacao t1 = new Transacao();
        t1.setValor(new BigDecimal("1000.00"));
        t1.setMovimento(Movimento.CREDITO);

        when(transacaoRepository.findByContaIdOrderByDataDescIdDesc(contaId)).thenReturn(List.of(t1));

        ResumoSaldo resumo = transacaoService.calcularResumo(contaId);

        assertThat(resumo.saldo()).isEqualByComparingTo(new BigDecimal("1000.00"));
        assertThat(resumo.totalCreditos()).isEqualByComparingTo(new BigDecimal("1000.00"));
        assertThat(resumo.totalDebitos()).isEqualByComparingTo(BigDecimal.ZERO);
    }

    @Test
    void calculaMapaDeSaldosParaMultiplasContas() {
        Conta conta1 = new Conta();
        conta1.setId(1L);

        Conta conta2 = new Conta();
        conta2.setId(2L);

        Transacao c1t1 = new Transacao();
        c1t1.setValor(new BigDecimal("800.00"));
        c1t1.setMovimento(Movimento.CREDITO);

        Transacao c2t1 = new Transacao();
        c2t1.setValor(new BigDecimal("200.00"));
        c2t1.setMovimento(Movimento.DEBITO);

        when(transacaoRepository.findByContaIdOrderByDataDescIdDesc(1L)).thenReturn(List.of(c1t1));
        when(transacaoRepository.findByContaIdOrderByDataDescIdDesc(2L)).thenReturn(List.of(c2t1));

        Map<Long, BigDecimal> mapaSaldos = transacaoService.calcularSaldosPorContas(List.of(conta1, conta2));

        assertThat(mapaSaldos).containsEntry(1L, new BigDecimal("800.00"));
        assertThat(mapaSaldos).containsEntry(2L, new BigDecimal("-200.00"));
    }
}
