package br.edu.ifpb.pweb2.intelliwallet.repository;

import java.util.List;
import java.util.Optional;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;

import br.edu.ifpb.pweb2.intelliwallet.model.Conta;

public interface ContaRepository extends JpaRepository<Conta, Long> {

    List<Conta> findByCorrentistaIdOrderByIdAsc(Long correntistaId);

    Page<Conta> findByCorrentistaIdOrderByIdAsc(Long correntistaId, Pageable pageable);

    Optional<Conta> findByCorrentistaIdAndNumero(Long correntistaId, String numero);
}
