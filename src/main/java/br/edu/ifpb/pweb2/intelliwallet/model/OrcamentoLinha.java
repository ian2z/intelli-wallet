package br.edu.ifpb.pweb2.intelliwallet.model;

import java.math.BigDecimal;
import java.util.List;

public record OrcamentoLinha(
        Long categoriaId,
        String categoriaNome,
        Natureza natureza,
        int ordem,
        boolean ativa,
        List<BigDecimal> meses,
        BigDecimal totalAnual
) {
}
