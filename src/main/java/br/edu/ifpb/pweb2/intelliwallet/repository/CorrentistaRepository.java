package br.edu.ifpb.pweb2.intelliwallet.repository;

import java.util.List;
import java.util.Optional;

import org.springframework.data.jpa.repository.JpaRepository;

import br.edu.ifpb.pweb2.intelliwallet.model.Correntista;
import br.edu.ifpb.pweb2.intelliwallet.model.Papel;

public interface CorrentistaRepository extends JpaRepository<Correntista, Long> {

    Optional<Correntista> findByLogin(String login);

    Optional<Correntista> findByLoginIgnoreCase(String login);

    boolean existsByLogin(String login);

    boolean existsByLoginIgnoreCase(String login);

    List<Correntista> findByPapel(Papel papel);

    List<Correntista> findByBloqueado(boolean bloqueado);
}

