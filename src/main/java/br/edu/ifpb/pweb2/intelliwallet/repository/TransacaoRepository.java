package br.edu.ifpb.pweb2.intelliwallet.repository;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.List;
import java.util.Optional;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.EntityGraph;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import br.edu.ifpb.pweb2.intelliwallet.model.Movimento;
import br.edu.ifpb.pweb2.intelliwallet.model.Transacao;

@Repository
public interface TransacaoRepository extends JpaRepository<Transacao, Long> {

    boolean existsByContaIdAndDescricaoAndData(Long contaId, String descricao, LocalDate data);

    long countByContaId(Long contaId);

    @EntityGraph(attributePaths = {"categoria", "comentario"})
    List<Transacao> findByContaIdOrderByDataDescIdDesc(Long contaId);

    @EntityGraph(attributePaths = {"categoria", "comentario"})
    Page<Transacao> findByContaIdAndDataBetweenOrderByDataAscIdAsc(
            Long contaId, LocalDate inicio, LocalDate fim, Pageable pageable);

    @EntityGraph(attributePaths = {"categoria", "comentario"})
    Optional<Transacao> findByIdAndContaId(Long id, Long contaId);

    @Query("""
            SELECT COALESCE(SUM(
                CASE WHEN t.movimento = br.edu.ifpb.pweb2.intelliwallet.model.Movimento.CREDITO
                     THEN t.valor ELSE -t.valor END
            ), 0)
            FROM Transacao t
            WHERE t.conta.id = :contaId AND t.data < :dataLimite
            """)
    BigDecimal calcularSaldoAnterior(@Param("contaId") Long contaId, @Param("dataLimite") LocalDate dataLimite);

    @Query("""
            SELECT COALESCE(SUM(
                CASE WHEN t.movimento = br.edu.ifpb.pweb2.intelliwallet.model.Movimento.CREDITO
                     THEN t.valor ELSE -t.valor END
            ), 0)
            FROM Transacao t
            WHERE t.conta.id = :contaId
              AND t.data >= :inicio
              AND (t.data < :dataCorte OR (t.data = :dataCorte AND t.id < :idCorte))
            """)
    BigDecimal calcularSaldoNoPeriodoAntesDe(
            @Param("contaId") Long contaId,
            @Param("inicio") LocalDate inicio,
            @Param("dataCorte") LocalDate dataCorte,
            @Param("idCorte") Long idCorte);

    @Query("""
            SELECT COALESCE(SUM(t.valor), 0)
            FROM Transacao t
            WHERE t.conta.id = :contaId
              AND t.data BETWEEN :inicio AND :fim
              AND t.movimento = :movimento
            """)
    BigDecimal somarPorContaPeriodoEMovimento(
            @Param("contaId") Long contaId,
            @Param("inicio") LocalDate inicio,
            @Param("fim") LocalDate fim,
            @Param("movimento") Movimento movimento);

    @Query("""
            SELECT t.categoria.id, MONTH(t.data), COALESCE(SUM(t.valor), 0)
            FROM Transacao t
            WHERE t.conta.correntista.id = :correntistaId
              AND YEAR(t.data) = :ano
              AND (:contaId IS NULL OR t.conta.id = :contaId)
            GROUP BY t.categoria.id, MONTH(t.data)
            """)
    List<Object[]> somarPorCategoriaEMes(
            @Param("correntistaId") Long correntistaId,
            @Param("ano") int ano,
            @Param("contaId") Long contaId);

    @Query("""
            SELECT DISTINCT YEAR(t.data)
            FROM Transacao t
            WHERE t.conta.correntista.id = :correntistaId
            ORDER BY YEAR(t.data) DESC
            """)
    List<Integer> listarAnosComTransacoes(@Param("correntistaId") Long correntistaId);
}
