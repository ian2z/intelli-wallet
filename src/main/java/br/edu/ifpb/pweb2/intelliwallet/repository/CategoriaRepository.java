package br.edu.ifpb.pweb2.intelliwallet.repository;

import java.util.List;
import java.util.Optional;

import org.springframework.data.jpa.repository.JpaRepository;

import br.edu.ifpb.pweb2.intelliwallet.model.Categoria;
import br.edu.ifpb.pweb2.intelliwallet.model.Natureza;

public interface CategoriaRepository extends JpaRepository<Categoria, Long> {

    List<Categoria> findByNaturezaOrderByOrdemAsc(Natureza natureza);

    // UC03: só categorias ativas aparecem no formulário de transação
    List<Categoria> findByAtivoTrueOrderByOrdemAsc();

    Optional<Categoria> findByNome(String nome);
}
