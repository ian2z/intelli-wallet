package br.edu.ifpb.pweb2.intelliwallet.model;

import java.math.BigDecimal;
import java.time.LocalDate;

import org.springframework.format.annotation.DateTimeFormat;

import jakarta.validation.constraints.DecimalMin;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;
import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
public class TransacaoForm {

    @NotNull(message = "Informe a data")
    @DateTimeFormat(iso = DateTimeFormat.ISO.DATE)
    private LocalDate data;

    @NotBlank(message = "Informe a descrição")
    @Size(max = 120, message = "A descrição deve ter até 120 caracteres")
    private String descricao;

    @NotNull(message = "Informe o valor")
    @DecimalMin(value = "0.01", message = "O valor deve ser maior que zero")
    private BigDecimal valor;

    @NotNull(message = "Selecione o movimento")
    private Movimento movimento;

    @NotNull(message = "Selecione uma categoria")
    private Long categoriaId;

    @Size(max = 255, message = "O comentário deve ter até 255 caracteres")
    private String comentario;
}
