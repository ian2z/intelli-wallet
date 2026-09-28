package br.edu.ifpb.pweb2.intelliwallet.repository;

import java.time.LocalDate;
import java.util.List;
import java.util.Optional;

import org.springframework.data.jpa.repository.EntityGraph;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import br.edu.ifpb.pweb2.intelliwallet.model.Transacao;

@Repository
public interface TransacaoRepository extends JpaRepository<Transacao, Long> {

    boolean existsByContaIdAndDescricaoAndData(Long contaId, String descricao, LocalDate data);

    long countByContaId(Long contaId);

    @EntityGraph(attributePaths = {"categoria", "comentario"})
    List<Transacao> findByContaIdOrderByDataDescIdDesc(Long contaId);

    @EntityGraph(attributePaths = {"categoria", "comentario"})
    Optional<Transacao> findByIdAndContaId(Long id, Long contaId);
}
