package br.edu.ifpb.pweb2.intelliwallet.repository;

import java.util.Optional;

import org.springframework.data.jpa.repository.JpaRepository;

import br.edu.ifpb.pweb2.intelliwallet.model.Correntista;

public interface CorrentistaRepository extends JpaRepository<Correntista, Long> {

    Optional<Correntista> findByLoginIgnoreCase(String login);

    boolean existsByLoginIgnoreCase(String login);
}
