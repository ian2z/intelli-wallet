package br.edu.ifpb.pweb2.intelliwallet.repository;

import java.util.List;

import org.springframework.data.jpa.repository.JpaRepository;

import br.edu.ifpb.pweb2.intelliwallet.model.Conta;

public interface ContaRepository extends JpaRepository<Conta, Long> {

    List<Conta> findByCorrentistaIdOrderByIdAsc(Long correntistaId);

    boolean existsByCorrentistaIdAndNumeroIgnoreCase(Long correntistaId, String numero);
}
