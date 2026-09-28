package br.edu.ifpb.pweb2.intelliwallet.service;

import java.util.ArrayList;
import java.util.Comparator;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.util.StringUtils;
import org.springframework.web.server.ResponseStatusException;

import br.edu.ifpb.pweb2.intelliwallet.model.Categoria;
import br.edu.ifpb.pweb2.intelliwallet.model.Comentario;
import br.edu.ifpb.pweb2.intelliwallet.model.Conta;
import br.edu.ifpb.pweb2.intelliwallet.model.Natureza;
import br.edu.ifpb.pweb2.intelliwallet.model.Transacao;
import br.edu.ifpb.pweb2.intelliwallet.model.TransacaoForm;
import br.edu.ifpb.pweb2.intelliwallet.repository.CategoriaRepository;
import br.edu.ifpb.pweb2.intelliwallet.repository.ContaRepository;
import br.edu.ifpb.pweb2.intelliwallet.repository.TransacaoRepository;

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
        verificarNaoBloqueado(conta);
        Categoria categoria = buscarCategoriaPermitida(formulario.getCategoriaId(), null);

        Transacao transacao = new Transacao(formulario.getData(), formulario.getDescricao().trim(),
                formulario.getValor(), formulario.getMovimento(), categoria, conta);

        if (StringUtils.hasText(formulario.getComentario())) {
            transacao.setComentario(new Comentario(formulario.getComentario().trim(), transacao));
        }
        return transacaoRepository.save(transacao); // o cascade grava o comentário junto
    }

    // UC04, passo 3: formulário preenchido com os dados atuais da transação
    @Transactional(readOnly = true)
    public TransacaoForm formularioDeEdicao(Long contaId, Long transacaoId) {
        Transacao transacao = buscarTransacao(contaId, transacaoId);

        TransacaoForm formulario = new TransacaoForm();
        formulario.setData(transacao.getData());
        formulario.setDescricao(transacao.getDescricao());
        formulario.setValor(transacao.getValor());
        formulario.setMovimento(transacao.getMovimento());
        formulario.setCategoriaId(transacao.getCategoria().getId());
        return formulario;
    }

    // Categoria atual da transação (usada para manter no <select> uma categoria já desativada)
    @Transactional(readOnly = true)
    public Long categoriaAtual(Long contaId, Long transacaoId) {
        return buscarTransacao(contaId, transacaoId).getCategoria().getId();
    }

    // UC04, passos 4 e 5: "tudo pode ser mudado"
    @Transactional
    public Transacao atualizar(Long contaId, Long transacaoId, TransacaoForm formulario) {
        Transacao transacao = buscarTransacao(contaId, transacaoId);
        verificarNaoBloqueado(transacao.getConta());

        Categoria categoria = buscarCategoriaPermitida(formulario.getCategoriaId(),
                transacao.getCategoria().getId());

        // Altera a entidade carregada do banco, campo a campo. O comentário não é tocado
        // aqui (ele tem formulário próprio, UC05/UC06), então nunca é apagado sem querer.
        transacao.setData(formulario.getData());
        transacao.setDescricao(formulario.getDescricao().trim());
        transacao.setValor(formulario.getValor());
        transacao.setMovimento(formulario.getMovimento());
        transacao.setCategoria(categoria);

        return transacao;
    }

    // Categorias do <select> do UC03: só as ativas, agrupadas por natureza e ordem
    @Transactional(readOnly = true)
    public Map<Natureza, List<Categoria>> categoriasAtivasPorNatureza() {
        return categoriasPorNatureza(null);
    }

    // UC04: as ativas + a categoria atual da transação, mesmo que tenha sido desativada depois
    @Transactional(readOnly = true)
    public Map<Natureza, List<Categoria>> categoriasPorNatureza(Long categoriaAtualId) {
        List<Categoria> categorias = new ArrayList<>(categoriaRepository.findByAtivoTrueOrderByOrdemAsc());
        if (categoriaAtualId != null && categorias.stream().noneMatch(c -> c.getId().equals(categoriaAtualId))) {
            categoriaRepository.findById(categoriaAtualId).ifPresent(categorias::add);
            categorias.sort(Comparator.comparingInt(Categoria::getOrdem));
        }

        Map<Natureza, List<Categoria>> porNatureza = new LinkedHashMap<>();
        for (Natureza natureza : Natureza.values()) {
            List<Categoria> daNatureza = categorias.stream()
                    .filter(categoria -> categoria.getNatureza() == natureza)
                    .toList();
            if (!daNatureza.isEmpty()) {
                porNatureza.put(natureza, daNatureza);
            }
        }
        return porNatureza;
    }

    // Busca pelo par (transação, conta): se a transação não for desta conta, é 404.
    // Impede editar a transação de outra conta só trocando o id na URL.
    private Transacao buscarTransacao(Long contaId, Long transacaoId) {
        return transacaoRepository.findByIdAndContaId(transacaoId, contaId)
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "Transação não encontrada"));
    }

    private void verificarNaoBloqueado(Conta conta) {
        if (conta.getCorrentista().isBloqueado()) {
            throw new TransacaoInvalidaException(null,
                    "Este correntista está bloqueado e não pode registrar ou alterar transações");
        }
    }

    // Só aceita categorias ativas. Exceção: a categoria que a transação já tinha (UC04).
    // O <select> já filtra, mas o id pode ser alterado no navegador, por isso a regra fica aqui.
    private Categoria buscarCategoriaPermitida(Long categoriaId, Long categoriaAtualId) {
        return categoriaRepository.findById(categoriaId)
                .filter(categoria -> categoria.isAtivo() || categoria.getId().equals(categoriaAtualId))
                .orElseThrow(() -> new TransacaoInvalidaException("categoriaId", "Selecione uma categoria ativa"));
    }
}
