package br.edu.ifpb.pweb2.intelliwallet.service;

import br.edu.ifpb.pweb2.intelliwallet.model.Categoria;
import br.edu.ifpb.pweb2.intelliwallet.model.Comentario;
import br.edu.ifpb.pweb2.intelliwallet.model.Conta;
import br.edu.ifpb.pweb2.intelliwallet.model.Natureza;
import br.edu.ifpb.pweb2.intelliwallet.model.Transacao;
import br.edu.ifpb.pweb2.intelliwallet.model.TransacaoForm;
import br.edu.ifpb.pweb2.intelliwallet.repository.CategoriaRepository;
import br.edu.ifpb.pweb2.intelliwallet.repository.ContaRepository;
import br.edu.ifpb.pweb2.intelliwallet.repository.TransacaoRepository;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.util.StringUtils;
import org.springframework.web.server.ResponseStatusException;

import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

@Service
public class TransacaoService {

    private final TransacaoRepository transacaoRepository;
    private final ContaRepository contaRepository;
    private final CategoriaRepository categoriaRepository;

    public TransacaoService(TransacaoRepository transacaoRepository, ContaRepository contaRepository,
                            CategoriaRepository categoriaRepository) {
        this.transacaoRepository = transacaoRepository;
        this.contaRepository = contaRepository;
        this.categoriaRepository = categoriaRepository;
    }

    @Transactional(readOnly = true)
    public Conta buscarConta(Long contaId) {
        return contaRepository.findById(contaId)
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "Conta não encontrada"));
    }

    @Transactional(readOnly = true)
    public List<Transacao> listarPorConta(Long contaId) {
        return transacaoRepository.findByContaIdOrderByDataDescIdDesc(contaId);
    }

    // UC03 (e UC05, quando o comentário é informado já no cadastro)
    @Transactional
    public Transacao criar(Long contaId, TransacaoForm formulario) {
        Conta conta = buscarConta(contaId);

        // Pré-condição do UC03: correntista não bloqueado
        if (conta.getCorrentista().isBloqueado()) {
            throw new TransacaoInvalidaException(null,
                    "Este correntista está bloqueado e não pode registrar transações");
        }

        // O <select> só mostra categorias ativas, mas o id pode ser alterado no navegador:
        // por isso a regra também é verificada aqui.
        Categoria categoria = categoriaRepository.findById(formulario.getCategoriaId())
                .filter(Categoria::isAtivo)
                .orElseThrow(() -> new TransacaoInvalidaException("categoriaId", "Selecione uma categoria ativa"));

        Transacao transacao = new Transacao(formulario.getData(), formulario.getDescricao().trim(),
                formulario.getValor(), formulario.getMovimento(), categoria, conta);

        if (StringUtils.hasText(formulario.getComentario())) {
            transacao.setComentario(new Comentario(formulario.getComentario().trim(), transacao));
        }
        return transacaoRepository.save(transacao); // o cascade grava o comentário junto
    }

    // Categorias do <select>: só as ativas, agrupadas por natureza (E, S, I) e pela ordem
    @Transactional(readOnly = true)
    public Map<Natureza, List<Categoria>> categoriasAtivasPorNatureza() {
        List<Categoria> ativas = categoriaRepository.findByAtivoTrueOrderByOrdemAsc();

        Map<Natureza, List<Categoria>> porNatureza = new LinkedHashMap<>();
        for (Natureza natureza : Natureza.values()) {
            List<Categoria> daNatureza = ativas.stream()
                    .filter(categoria -> categoria.getNatureza() == natureza)
                    .toList();
            if (!daNatureza.isEmpty()) {
                porNatureza.put(natureza, daNatureza);
            }
        }
        return porNatureza;
    }
}