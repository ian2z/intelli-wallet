package br.edu.ifpb.pweb2.intelliwallet.model;

import java.math.BigDecimal;

public record ResumoSaldo(
        BigDecimal saldo,
        BigDecimal totalCreditos,
        BigDecimal totalDebitos
) {
    public ResumoSaldo {
        if (saldo == null) saldo = BigDecimal.ZERO;
        if (totalCreditos == null) totalCreditos = BigDecimal.ZERO;
        if (totalDebitos == null) totalDebitos = BigDecimal.ZERO;
    }

    public static ResumoSaldo vazio() {
        return new ResumoSaldo(BigDecimal.ZERO, BigDecimal.ZERO, BigDecimal.ZERO);
    }

    public boolean isNegativo() {
        return saldo.signum() < 0;
    }

    public boolean isPositivo() {
        return saldo.signum() >= 0;
    }
}
