package br.edu.ifpb.pweb2.intelliwallet.model;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Getter
@Setter
@NoArgsConstructor
public class CorrentistaForm {

    private Long id;

    @NotBlank(message = "Informe o nome")
    @Size(max = 120, message = "O nome deve ter até 120 caracteres")
    private String nome;

    @NotBlank(message = "Informe o login")
    @Size(min = 3, max = 60, message = "O login deve ter entre 3 e 60 caracteres")
    private String login;

    @Size(max = 72, message = "A senha deve ter até 72 caracteres")
    private String senha;

    private boolean bloqueado;

    private Papel papel = Papel.CORRENTISTA;

    public CorrentistaForm(Correntista correntista) {
        this.id = correntista.getId();
        this.nome = correntista.getNome();
        this.login = correntista.getLogin();
        this.bloqueado = correntista.isBloqueado();
        this.papel = correntista.getPapel();
    }
}
