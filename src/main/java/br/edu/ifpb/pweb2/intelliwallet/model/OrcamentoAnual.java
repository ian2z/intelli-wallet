package br.edu.ifpb.pweb2.intelliwallet.model;

import java.math.BigDecimal;
import java.util.List;

public record OrcamentoAnual(
        int ano,
        Long contaId,
        List<OrcamentoGrupo> grupos,
        BigDecimal totalEntradas,
        BigDecimal totalSaidas,
        BigDecimal totalInvestimentos,
        BigDecimal saldoAnual
) {
}
