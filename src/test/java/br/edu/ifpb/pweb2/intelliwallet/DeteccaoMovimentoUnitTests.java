package br.edu.ifpb.pweb2.intelliwallet;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.Optional;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import br.edu.ifpb.pweb2.intelliwallet.model.Categoria;
import br.edu.ifpb.pweb2.intelliwallet.model.Conta;
import br.edu.ifpb.pweb2.intelliwallet.model.Correntista;
import br.edu.ifpb.pweb2.intelliwallet.model.Movimento;
import br.edu.ifpb.pweb2.intelliwallet.model.Natureza;
import br.edu.ifpb.pweb2.intelliwallet.model.Transacao;
import br.edu.ifpb.pweb2.intelliwallet.model.TransacaoForm;
import br.edu.ifpb.pweb2.intelliwallet.repository.CategoriaRepository;
import br.edu.ifpb.pweb2.intelliwallet.repository.ContaRepository;
import br.edu.ifpb.pweb2.intelliwallet.repository.TransacaoRepository;
import br.edu.ifpb.pweb2.intelliwallet.service.TransacaoService;

@ExtendWith(MockitoExtension.class)
class DeteccaoMovimentoUnitTests {

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
    void mapeiaNaturezaParaMovimentoPadrao() {
        assertThat(Natureza.ENTRADA.getMovimentoPadrao()).isEqualTo(Movimento.CREDITO);
        assertThat(Natureza.SAIDA.getMovimentoPadrao()).isEqualTo(Movimento.DEBITO);
        assertThat(Natureza.INVESTIMENTO.getMovimentoPadrao()).isEqualTo(Movimento.DEBITO);
    }

    @Test
    void categoriaDeEntradaDefineMovimentoComoCredito() {
        Categoria salario = new Categoria();
        salario.setId(1L);
        salario.setNome("Salário");
        salario.setNatureza(Natureza.ENTRADA);
        salario.setAtivo(true);

        assertThat(salario.getMovimentoPadrao()).isEqualTo(Movimento.CREDITO);
    }

    @Test
    void categoriaDeSaidaOuInvestimentoDefineMovimentoComoDebito() {
        Categoria feira = new Categoria();
        feira.setId(2L);
        feira.setNome("Feira");
        feira.setNatureza(Natureza.SAIDA);
        feira.setAtivo(true);

        Categoria aporte = new Categoria();
        aporte.setId(3L);
        aporte.setNome("Aporte Renda Fixa");
        aporte.setNatureza(Natureza.INVESTIMENTO);
        aporte.setAtivo(true);

        assertThat(feira.getMovimentoPadrao()).isEqualTo(Movimento.DEBITO);
        assertThat(aporte.getMovimentoPadrao()).isEqualTo(Movimento.DEBITO);
    }

    @Test
    void criarTransacaoDetectaAutomaticamenteCreditoParaCategoriaDeEntrada() {
        Long contaId = 1L;
        Conta conta = new Conta();
        conta.setId(contaId);
        conta.setCorrentista(new Correntista());

        Categoria salario = new Categoria();
        salario.setId(10L);
        salario.setNome("Salário");
        salario.setNatureza(Natureza.ENTRADA);
        salario.setAtivo(true);

        TransacaoForm form = new TransacaoForm();
        form.setData(LocalDate.now());
        form.setDescricao("Depósito Salário");
        form.setValor(new BigDecimal("4000.00"));
        form.setCategoriaId(10L);
        // Mesmo que no form venha marcado DEBITO por engano, a regra de negócio corrige
        form.setMovimento(Movimento.DEBITO);

        when(contaRepository.findById(contaId)).thenReturn(Optional.of(conta));
        when(categoriaRepository.findById(10L)).thenReturn(Optional.of(salario));
        when(transacaoRepository.save(any(Transacao.class))).thenAnswer(i -> i.getArgument(0));

        Transacao criada = transacaoService.criar(contaId, form);

        assertThat(criada.getMovimento()).isEqualTo(Movimento.CREDITO);
        assertThat(form.getMovimento()).isEqualTo(Movimento.CREDITO);
    }

    @Test
    void criarTransacaoDetectaAutomaticamenteDebitoParaCategoriaDeSaida() {
        Long contaId = 1L;
        Conta conta = new Conta();
        conta.setId(contaId);
        conta.setCorrentista(new Correntista());

        Categoria feira = new Categoria();
        feira.setId(20L);
        feira.setNome("Feira");
        feira.setNatureza(Natureza.SAIDA);
        feira.setAtivo(true);

        TransacaoForm form = new TransacaoForm();
        form.setData(LocalDate.now());
        form.setDescricao("Compras do mês");
        form.setValor(new BigDecimal("350.00"));
        form.setCategoriaId(20L);
        form.setMovimento(Movimento.CREDITO); // incorreto no form propositalmente

        when(contaRepository.findById(contaId)).thenReturn(Optional.of(conta));
        when(categoriaRepository.findById(20L)).thenReturn(Optional.of(feira));
        when(transacaoRepository.save(any(Transacao.class))).thenAnswer(i -> i.getArgument(0));

        Transacao criada = transacaoService.criar(contaId, form);

        assertThat(criada.getMovimento()).isEqualTo(Movimento.DEBITO);
        assertThat(form.getMovimento()).isEqualTo(Movimento.DEBITO);
    }
}
