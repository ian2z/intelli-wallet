package br.edu.ifpb.pweb2.intelliwallet.repository;

import br.edu.ifpb.pweb2.intelliwallet.model.Transacao;
import org.springframework.data.jpa.repository.EntityGraph;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface TransacaoRepository extends JpaRepository<Transacao, Integer> {

    // Transações de uma conta. O @EntityGraph traz categoria e comentário
    @EntityGraph(attributePaths = {"categoria", "comentario"})
    List<Transacao> findByContaIdOrderByDataDescIdDesc(Integer contaId);

    // Garante que a transação pertence à conta informada
    @EntityGraph(attributePaths = {"categoria", "comentario"})
    Optional<Transacao> findByIdAndContaId(Integer id, Integer contaId);
}
