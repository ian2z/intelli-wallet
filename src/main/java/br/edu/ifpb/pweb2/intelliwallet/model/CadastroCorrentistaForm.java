package br.edu.ifpb.pweb2.intelliwallet.model;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;
import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
public class CadastroCorrentistaForm {

    @NotBlank(message = "Informe seu nome")
    @Size(max = 120, message = "O nome deve ter até 120 caracteres")
    private String nome;

    @NotBlank(message = "Informe um login")
    @Size(min = 3, max = 60, message = "O login deve ter entre 3 e 60 caracteres")
    private String login;

    @NotBlank(message = "Informe uma senha")
    @Size(min = 8, max = 72, message = "A senha deve ter entre 8 e 72 caracteres")
    private String senha;
}
