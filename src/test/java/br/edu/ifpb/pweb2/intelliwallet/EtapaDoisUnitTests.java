package br.edu.ifpb.pweb2.intelliwallet;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.when;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.List;
import java.util.Optional;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.PageRequest;

import br.edu.ifpb.pweb2.intelliwallet.model.Categoria;
import br.edu.ifpb.pweb2.intelliwallet.model.CategoriaForm;
import br.edu.ifpb.pweb2.intelliwallet.model.Conta;
import br.edu.ifpb.pweb2.intelliwallet.model.Correntista;
import br.edu.ifpb.pweb2.intelliwallet.model.ExtratoConta;
import br.edu.ifpb.pweb2.intelliwallet.model.Movimento;
import br.edu.ifpb.pweb2.intelliwallet.model.Natureza;
import br.edu.ifpb.pweb2.intelliwallet.model.OrcamentoAnual;
import br.edu.ifpb.pweb2.intelliwallet.model.Papel;
import br.edu.ifpb.pweb2.intelliwallet.model.Transacao;
import br.edu.ifpb.pweb2.intelliwallet.repository.CategoriaRepository;
import br.edu.ifpb.pweb2.intelliwallet.repository.ContaRepository;
import br.edu.ifpb.pweb2.intelliwallet.repository.CorrentistaRepository;
import br.edu.ifpb.pweb2.intelliwallet.repository.TransacaoRepository;
import br.edu.ifpb.pweb2.intelliwallet.security.CorrentistaUserDetails;
import br.edu.ifpb.pweb2.intelliwallet.service.CategoriaInvalidaException;
import br.edu.ifpb.pweb2.intelliwallet.service.CategoriaService;
import br.edu.ifpb.pweb2.intelliwallet.service.CorrentistaService;
import br.edu.ifpb.pweb2.intelliwallet.service.OrcamentoService;
import br.edu.ifpb.pweb2.intelliwallet.service.SenhaService;
import br.edu.ifpb.pweb2.intelliwallet.service.TransacaoService;

@ExtendWith(MockitoExtension.class)
class EtapaDoisUnitTests {

    @Mock
    private CategoriaRepository categoriaRepository;

    @Mock
    private CorrentistaRepository correntistaRepository;

    @Mock
    private ContaRepository contaRepository;

    @Mock
    private TransacaoRepository transacaoRepository;

    @Mock
    private SenhaService senhaService;

    @Test
    void uc22e23CategoriaServiceCriaEditaEDesativaSemExcluir() {
        CategoriaService service = new CategoriaService(categoriaRepository);

        CategoriaForm form = new CategoriaForm();
        form.setNome("  Streaming  ");
        form.setNatureza(Natureza.SAIDA);
        form.setOrdem(15);
        form.setAtivo(true);

        when(categoriaRepository.existsByNomeIgnoreCase("Streaming")).thenReturn(false);
        when(categoriaRepository.saveAndFlush(any(Categoria.class))).thenAnswer(i -> {
            Categoria c = i.getArgument(0);
            c.setId(99L);
            return c;
        });

        Categoria criada = service.criar(form);
        assertThat(criada.getNome()).isEqualTo("Streaming");
        assertThat(criada.isAtivo()).isTrue();

        when(categoriaRepository.findById(99L)).thenReturn(Optional.of(criada));
        Categoria desativada = service.alternarAtivo(99L);
        assertThat(desativada.isAtivo()).isFalse();

        when(categoriaRepository.existsByNomeIgnoreCase("Salário")).thenReturn(true);
        CategoriaForm duplicado = new CategoriaForm();
        duplicado.setNome("Salário");
        duplicado.setNatureza(Natureza.ENTRADA);
        duplicado.setOrdem(1);
        assertThatThrownBy(() -> service.criar(duplicado))
                .isInstanceOf(CategoriaInvalidaException.class);
    }

    @Test
    void uc24BloqueioImpedeAcessoNoUserDetails() {
        CorrentistaService service = new CorrentistaService(correntistaRepository, senhaService);
        Correntista correntista = new Correntista("João", "joao", "hash", Papel.CORRENTISTA);
        correntista.setId(5L);
        correntista.setBloqueado(false);

        when(correntistaRepository.findById(5L)).thenReturn(Optional.of(correntista));
        when(correntistaRepository.saveAndFlush(any(Correntista.class))).thenAnswer(i -> i.getArgument(0));

        Correntista bloqueado = service.alternarBloqueio(5L);
        assertThat(bloqueado.isBloqueado()).isTrue();

        CorrentistaUserDetails details = new CorrentistaUserDetails(bloqueado);
        assertThat(details.isAccountNonLocked()).isFalse();
        assertThat(details.getAuthorities()).extracting("authority").containsExactly("ROLE_CORRENTISTA");
    }

    @Test
    void uc07ExtratoCalculaSaldoInicialAcumuladoESaldoFinalComPaginacao() {
        TransacaoService service = new TransacaoService(transacaoRepository, contaRepository, categoriaRepository);
        Long contaId = 1L;
        LocalDate inicio = LocalDate.of(2026, 11, 1);
        LocalDate fim = LocalDate.of(2026, 11, 30);

        Conta conta = new Conta();
        conta.setId(contaId);

        Transacao t1 = new Transacao();
        t1.setId(10L);
        t1.setData(LocalDate.of(2026, 11, 5));
        t1.setValor(new BigDecimal("1000.00"));
        t1.setMovimento(Movimento.CREDITO);

        Transacao t2 = new Transacao();
        t2.setId(11L);
        t2.setData(LocalDate.of(2026, 11, 10));
        t2.setValor(new BigDecimal("250.00"));
        t2.setMovimento(Movimento.DEBITO);

        PageRequest pageable = PageRequest.of(0, 10);
        Page<Transacao> pagina = new PageImpl<>(List.of(t1, t2), pageable, 2);

        when(contaRepository.findById(contaId)).thenReturn(Optional.of(conta));
        when(transacaoRepository.calcularSaldoAnterior(contaId, inicio)).thenReturn(new BigDecimal("500.00"));
        when(transacaoRepository.somarPorContaPeriodoEMovimento(contaId, inicio, fim, Movimento.CREDITO))
                .thenReturn(new BigDecimal("1000.00"));
        when(transacaoRepository.somarPorContaPeriodoEMovimento(contaId, inicio, fim, Movimento.DEBITO))
                .thenReturn(new BigDecimal("250.00"));
        when(transacaoRepository.findByContaIdAndDataBetweenOrderByDataAscIdAsc(contaId, inicio, fim, pageable))
                .thenReturn(pagina);

        ExtratoConta extrato = service.gerarExtrato(contaId, inicio, fim, pageable);

        assertThat(extrato.saldoInicial()).isEqualByComparingTo("500.00");
        assertThat(extrato.saldoFinal()).isEqualByComparingTo("1250.00");
        assertThat(extrato.itens()).hasSize(2);
        assertThat(extrato.itens().get(0).saldoApos()).isEqualByComparingTo("1500.00");
        assertThat(extrato.itens().get(1).saldoApos()).isEqualByComparingTo("1250.00");
    }

    @Test
    void uc08OrcamentoAnualAgrupaPorNaturezaEOrdemETotalizaMesesEAno() {
        OrcamentoService service = new OrcamentoService(categoriaRepository, transacaoRepository);

        Categoria salario = new Categoria();
        salario.setId(1L);
        salario.setNome("Salário");
        salario.setNatureza(Natureza.ENTRADA);
        salario.setOrdem(1);
        salario.setAtivo(true);

        Categoria feira = new Categoria();
        feira.setId(2L);
        feira.setNome("Feira");
        feira.setNatureza(Natureza.SAIDA);
        feira.setOrdem(2);
        feira.setAtivo(true);

        Categoria saude = new Categoria();
        saude.setId(3L);
        saude.setNome("Saúde");
        saude.setNatureza(Natureza.SAIDA);
        saude.setOrdem(1);
        saude.setAtivo(true);

        Categoria aporte = new Categoria();
        aporte.setId(4L);
        aporte.setNome("Aporte Renda Fixa");
        aporte.setNatureza(Natureza.INVESTIMENTO);
        aporte.setOrdem(1);
        aporte.setAtivo(true);

        when(categoriaRepository.findAllByOrderByOrdemAscNomeAsc())
                .thenReturn(List.of(salario, feira, saude, aporte));
        when(transacaoRepository.somarPorCategoriaEMes(eq(1L), eq(2026), eq(null)))
                .thenReturn(List.of(
                        new Object[] {1L, 1, new BigDecimal("5000.00")},
                        new Object[] {1L, 2, new BigDecimal("5000.00")},
                        new Object[] {3L, 1, new BigDecimal("300.00")},
                        new Object[] {2L, 2, new BigDecimal("700.00")},
                        new Object[] {4L, 2, new BigDecimal("1000.00")}
                ));

        OrcamentoAnual orcamento = service.gerarOrcamento(1L, 2026, null);

        assertThat(orcamento.grupos()).extracting(g -> g.natureza())
                .containsExactly(Natureza.ENTRADA, Natureza.SAIDA, Natureza.INVESTIMENTO);
        // Dentro de SAIDA, ordem 1 (Saúde) deve vir antes de ordem 2 (Feira)
        assertThat(orcamento.grupos().get(1).linhas()).extracting(l -> l.categoriaNome())
                .containsExactly("Saúde", "Feira");

        assertThat(orcamento.totalEntradas()).isEqualByComparingTo("10000.00");
        assertThat(orcamento.totalSaidas()).isEqualByComparingTo("1000.00");
        assertThat(orcamento.totalInvestimentos()).isEqualByComparingTo("1000.00");
        assertThat(orcamento.saldoAnual()).isEqualByComparingTo("8000.00");
    }
}
