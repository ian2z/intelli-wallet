package br.edu.ifpb.pweb2.intelliwallet.model;

import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Getter
@Setter
@NoArgsConstructor
public class CategoriaForm {

    private Long id;

    @NotBlank(message = "Informe o nome da categoria")
    @Size(max = 80, message = "O nome deve ter até 80 caracteres")
    private String nome;

    @NotNull(message = "Selecione a natureza da categoria")
    private Natureza natureza;

    @NotNull(message = "Informe a ordem de exibição")
    @Min(value = 1, message = "A ordem deve ser maior ou igual a 1")
    private Integer ordem = 1;

    private boolean ativo = true;

    public CategoriaForm(Categoria categoria) {
        this.id = categoria.getId();
        this.nome = categoria.getNome();
        this.natureza = categoria.getNatureza();
        this.ordem = categoria.getOrdem();
        this.ativo = categoria.isAtivo();
    }
}
