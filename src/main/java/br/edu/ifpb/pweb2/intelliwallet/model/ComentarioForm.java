package br.edu.ifpb.pweb2.intelliwallet.model;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;
import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
public class ComentarioForm {

    @NotBlank(message = "Informe o texto do comentário")
    @Size(max = 500, message = "O comentário deve ter até 500 caracteres")
    private String texto;

    private String voltarPara;
}
