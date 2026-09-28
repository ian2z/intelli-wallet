package br.edu.ifpb.pweb2.intelliwallet.repository;

import java.util.Optional;

import br.edu.ifpb.pweb2.intelliwallet.model.Comentario;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface ComentarioRepository extends JpaRepository<Comentario, Integer> {

    Optional<Comentario> findByTransacaoId(Long transacaoId);
}