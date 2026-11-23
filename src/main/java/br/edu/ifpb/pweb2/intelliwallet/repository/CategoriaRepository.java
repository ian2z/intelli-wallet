package br.edu.ifpb.pweb2.intelliwallet.repository;

import java.util.List;
import java.util.Optional;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;

import br.edu.ifpb.pweb2.intelliwallet.model.Categoria;
import br.edu.ifpb.pweb2.intelliwallet.model.Natureza;

public interface CategoriaRepository extends JpaRepository<Categoria, Long> {

    List<Categoria> findByNaturezaOrderByOrdemAsc(Natureza natureza);

    // UC03: só categorias ativas aparecem no formulário de transação
    List<Categoria> findByAtivoTrueOrderByOrdemAsc();

    List<Categoria> findAllByOrderByOrdemAscNomeAsc();

    Optional<Categoria> findByNome(String nome);

    boolean existsByNomeIgnoreCase(String nome);

    boolean existsByNomeIgnoreCaseAndIdNot(String nome, Long id);

    @Query("""
            SELECT c FROM Categoria c
            ORDER BY CASE c.natureza
                WHEN br.edu.ifpb.pweb2.intelliwallet.model.Natureza.ENTRADA THEN 1
                WHEN br.edu.ifpb.pweb2.intelliwallet.model.Natureza.SAIDA THEN 2
                ELSE 3
            END ASC, c.ordem ASC, c.nome ASC
            """)
    Page<Categoria> listarOrdenadas(Pageable pageable);
}
