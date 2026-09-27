package br.edu.ifpb.pweb2.intelliwallet.model;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;
import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
public class ContaForm {

    @NotBlank(message = "Informe o número da conta")
    @Size(max = 40, message = "O número deve ter até 40 caracteres")
    private String numero;

    @NotBlank(message = "Informe uma descrição")
    @Size(max = 120, message = "A descrição deve ter até 120 caracteres")
    private String descricao;

    @NotNull(message = "Escolha o tipo da conta")
    private TipoConta tipo;

    private Integer diaFechamento;
}
