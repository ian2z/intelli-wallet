package br.edu.ifpb.pweb2.intelliwallet.model;

import java.util.ArrayList;
import java.util.List;

import jakarta.persistence.CascadeType;
import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.OneToMany;
import jakarta.persistence.Table;
import jakarta.validation.constraints.NotBlank;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Entity
@Table(name = "correntistas")
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
public class Correntista {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @NotBlank
    @Column(nullable = false)
    private String nome;

    @NotBlank
    @Column(nullable = false, unique = true)
    private String login;

    @NotBlank
    @Column(name = "senha_hash", nullable = false)
    private String senha;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    private Papel papel = Papel.CORRENTISTA;

    @Column(nullable = false)
    private boolean bloqueado;

    @OneToMany(mappedBy = "correntista", cascade = CascadeType.ALL, orphanRemoval = true)
    private List<Conta> contas = new ArrayList<>();

    public Correntista(String nome, String login, String senha, Papel papel) {
        this.nome = nome;
        this.login = login;
        this.senha = senha;
        this.papel = papel;
    }

    public void adicionarConta(Conta conta) {
        contas.add(conta);
        conta.setCorrentista(this);
    }

    public void removerConta(Conta conta) {
        contas.remove(conta);
        conta.setCorrentista(null);
    }
}

