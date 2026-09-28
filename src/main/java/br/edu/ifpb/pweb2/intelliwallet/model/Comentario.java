package br.edu.ifpb.pweb2.intelliwallet.model;

import jakarta.persistence.*;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.io.Serializable;

@Entity
@Table(name = "comentarios")
@Getter
@Setter
@NoArgsConstructor
public class Comentario implements Serializable {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Integer id;

    @Column(nullable = false, length = 1000)
    private String texto;

    // Lado dono da relação: é aqui que fica a FK transacao_id
    @OneToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "transacao_id", unique = true)
    private Transacao transacao;

    public Comentario(String texto, Transacao transacao) {
        this.texto = texto;
        this.transacao = transacao;
    }
}