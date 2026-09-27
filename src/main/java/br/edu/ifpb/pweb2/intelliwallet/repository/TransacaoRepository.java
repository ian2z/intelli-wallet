package br.edu.ifpb.pweb2.intelliwallet.repository;

import java.time.LocalDate;

import org.springframework.data.jpa.repository.JpaRepository;

import br.edu.ifpb.pweb2.intelliwallet.model.Transacao;

public interface TransacaoRepository extends JpaRepository<Transacao, Long> {

    boolean existsByContaIdAndDescricaoAndData(Long contaId, String descricao, LocalDate data);

    long countByContaId(Long contaId);
}
