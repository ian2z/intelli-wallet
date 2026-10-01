package br.edu.ifpb.pweb2.intelliwallet.model;

import jakarta.validation.constraints.Max;
import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;
import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
public class ContaForm {

    @NotBlank(message = "Informe o número da conta ou do cartão")
    @Size(max = 255, message = "Use no máximo 255 caracteres")
    private String numero;

    @NotBlank(message = "Informe uma descrição")
    @Size(max = 255, message = "Use no máximo 255 caracteres")
    private String descricao;

    @NotNull(message = "Selecione o tipo da conta")
    private TipoConta tipo;

    @Min(value = 1, message = "Informe um dia entre 1 e 31")
    @Max(value = 31, message = "Informe um dia entre 1 e 31")
    private Integer diaFechamento;
}
