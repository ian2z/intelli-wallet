package br.edu.ifpb.pweb2.intelliwallet.service;

import java.math.BigDecimal;
import java.time.Year;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Comparator;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.TreeSet;

import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import br.edu.ifpb.pweb2.intelliwallet.model.Categoria;
import br.edu.ifpb.pweb2.intelliwallet.model.Natureza;
import br.edu.ifpb.pweb2.intelliwallet.model.OrcamentoAnual;
import br.edu.ifpb.pweb2.intelliwallet.model.OrcamentoGrupo;
import br.edu.ifpb.pweb2.intelliwallet.model.OrcamentoLinha;
import br.edu.ifpb.pweb2.intelliwallet.repository.CategoriaRepository;
import br.edu.ifpb.pweb2.intelliwallet.repository.TransacaoRepository;

@Service
public class OrcamentoService {

    private final CategoriaRepository categoriaRepository;
    private final TransacaoRepository transacaoRepository;

    public OrcamentoService(CategoriaRepository categoriaRepository, TransacaoRepository transacaoRepository) {
        this.categoriaRepository = categoriaRepository;
        this.transacaoRepository = transacaoRepository;
    }

    @Transactional(readOnly = true)
    public OrcamentoAnual gerarOrcamento(Long correntistaId, int ano, Long contaId) {
        List<Categoria> todasCategorias = categoriaRepository.findAllByOrderByOrdemAscNomeAsc();
        List<Object[]> agregados = transacaoRepository.somarPorCategoriaEMes(correntistaId, ano, contaId);

        Map<Long, BigDecimal[]> valoresPorCategoria = new HashMap<>();
        for (Object[] linha : agregados) {
            Long categoriaId = ((Number) linha[0]).longValue();
            int mes = ((Number) linha[1]).intValue(); // 1..12
            BigDecimal soma = linha[2] instanceof BigDecimal bd ? bd : new BigDecimal(linha[2].toString());
            if (mes >= 1 && mes <= 12) {
                valoresPorCategoria
                        .computeIfAbsent(categoriaId, k -> mesesZerados())[mes - 1] = soma;
            }
        }

        List<Natureza> ordemNaturezas = List.of(Natureza.ENTRADA, Natureza.SAIDA, Natureza.INVESTIMENTO);
        List<OrcamentoGrupo> grupos = new ArrayList<>();
        BigDecimal totalEntradas = BigDecimal.ZERO;
        BigDecimal totalSaidas = BigDecimal.ZERO;
        BigDecimal totalInvestimentos = BigDecimal.ZERO;

        for (Natureza natureza : ordemNaturezas) {
            List<Categoria> categoriasDaNatureza = todasCategorias.stream()
                    .filter(c -> c.getNatureza() == natureza)
                    .filter(c -> c.isAtivo() || valoresPorCategoria.containsKey(c.getId()))
                    .sorted(Comparator.comparingInt(Categoria::getOrdem).thenComparing(Categoria::getNome))
                    .toList();

            BigDecimal[] totaisGrupo = mesesZerados();
            BigDecimal totalAnualGrupo = BigDecimal.ZERO;
            List<OrcamentoLinha> linhas = new ArrayList<>();

            for (Categoria categoria : categoriasDaNatureza) {
                BigDecimal[] meses = valoresPorCategoria.getOrDefault(categoria.getId(), mesesZerados());
                BigDecimal totalLinha = BigDecimal.ZERO;
                for (int i = 0; i < 12; i++) {
                    totalLinha = totalLinha.add(meses[i]);
                    totaisGrupo[i] = totaisGrupo[i].add(meses[i]);
                }
                totalAnualGrupo = totalAnualGrupo.add(totalLinha);
                linhas.add(new OrcamentoLinha(
                        categoria.getId(),
                        categoria.getNome(),
                        natureza,
                        categoria.getOrdem(),
                        categoria.isAtivo(),
                        Arrays.asList(meses),
                        totalLinha
                ));
            }

            grupos.add(new OrcamentoGrupo(natureza, linhas, Arrays.asList(totaisGrupo), totalAnualGrupo));
            if (natureza == Natureza.ENTRADA) {
                totalEntradas = totalAnualGrupo;
            } else if (natureza == Natureza.SAIDA) {
                totalSaidas = totalAnualGrupo;
            } else {
                totalInvestimentos = totalAnualGrupo;
            }
        }

        BigDecimal saldoAnual = totalEntradas.subtract(totalSaidas).subtract(totalInvestimentos);
        return new OrcamentoAnual(ano, contaId, grupos, totalEntradas, totalSaidas, totalInvestimentos, saldoAnual);
    }

    @Transactional(readOnly = true)
    public List<Integer> listarAnosDisponiveis(Long correntistaId, int anoSelecionado) {
        Set<Integer> anos = new TreeSet<>(Comparator.reverseOrder());
        anos.add(Year.now().getValue());
        anos.add(anoSelecionado);
        anos.addAll(transacaoRepository.listarAnosComTransacoes(correntistaId));
        return new ArrayList<>(anos);
    }

    private BigDecimal[] mesesZerados() {
        BigDecimal[] meses = new BigDecimal[12];
        Arrays.fill(meses, BigDecimal.ZERO);
        return meses;
    }
}
