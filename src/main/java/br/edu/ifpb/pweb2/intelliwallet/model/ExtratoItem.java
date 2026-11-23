package br.edu.ifpb.pweb2.intelliwallet.model;

import java.math.BigDecimal;

public record ExtratoItem(
        Transacao transacao,
        BigDecimal saldoApos
) {
}
