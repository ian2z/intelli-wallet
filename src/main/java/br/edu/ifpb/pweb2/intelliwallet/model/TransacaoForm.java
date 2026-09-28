package br.edu.ifpb.pweb2.intelliwallet.model;

import java.math.BigDecimal;
import java.time.LocalDate;

import org.springframework.format.annotation.DateTimeFormat;

import jakarta.validation.constraints.DecimalMin;
import jakarta.validation.constraints.Digits;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;
import lombok.Getter;
import lombok.Setter;

// Dados do formulário do UC03, no mesmo padrão do CadastroCorrentistaForm.
@Getter
@Setter
public class TransacaoForm {

    @NotNull(message = "Informe a data")
    @DateTimeFormat(iso = DateTimeFormat.ISO.DATE)
    private LocalDate data = LocalDate.now();

    @NotBlank(message = "Informe a descrição")
    @Size(max = 150, message = "A descrição deve ter até 150 caracteres")
    private String descricao;

    @NotNull(message = "Informe o valor")
    @DecimalMin(value = "0.01", message = "O valor deve ser maior que zero")
    @Digits(integer = 10, fraction = 2, message = "Use no máximo duas casas decimais")
    private BigDecimal valor;

    @NotNull(message = "Selecione crédito ou débito")
    private Movimento movimento = Movimento.DEBITO;

    @NotNull(message = "Selecione uma categoria")
    private Long categoriaId;

    // Opcional: comentário já no cadastro (UC05)
    @Size(max = 1000, message = "O comentário deve ter até 1000 caracteres")
    private String comentario;
}
