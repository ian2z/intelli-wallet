package br.edu.ifpb.pweb2.intelliwallet.model;

import java.math.BigDecimal;
import java.util.List;

public record OrcamentoGrupo(
        Natureza natureza,
        List<OrcamentoLinha> linhas,
        List<BigDecimal> totaisMensais,
        BigDecimal totalAnual
) {
}
