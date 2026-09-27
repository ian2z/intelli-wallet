package br.edu.ifpb.pweb2.intelliwallet.repository;

import java.util.Optional;
import java.util.List;

import org.springframework.data.jpa.repository.JpaRepository;

import br.edu.ifpb.pweb2.intelliwallet.model.Correntista;
import br.edu.ifpb.pweb2.intelliwallet.model.Papel;

public interface CorrentistaRepository extends JpaRepository<Correntista, Long> {

    Optional<Correntista> findByLoginIgnoreCase(String login);

    boolean existsByLoginIgnoreCase(String login);

    List<Correntista> findByPapelAndBloqueadoFalseOrderByNomeAsc(Papel papel);
}
