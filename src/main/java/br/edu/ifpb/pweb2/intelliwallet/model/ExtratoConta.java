package br.edu.ifpb.pweb2.intelliwallet.model;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.List;

import org.springframework.data.domain.Page;

public record ExtratoConta(
        Conta conta,
        LocalDate dataInicio,
        LocalDate dataFim,
        BigDecimal saldoInicial,
        BigDecimal saldoFinal,
        BigDecimal totalCreditosPeriodo,
        BigDecimal totalDebitosPeriodo,
        List<ExtratoItem> itens,
        Page<Transacao> pagina
) {
}
