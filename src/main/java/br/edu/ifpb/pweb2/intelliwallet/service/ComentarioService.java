package br.edu.ifpb.pweb2.intelliwallet.service;

import org.springframework.dao.DataIntegrityViolationException;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.Optional;

import br.edu.ifpb.pweb2.intelliwallet.model.Comentario;
import br.edu.ifpb.pweb2.intelliwallet.model.ComentarioForm;
import br.edu.ifpb.pweb2.intelliwallet.model.Transacao;
import br.edu.ifpb.pweb2.intelliwallet.repository.ComentarioRepository;

@Service
public class ComentarioService {

    private final ComentarioRepository comentarioRepository;

    public ComentarioService(ComentarioRepository comentarioRepository) {
        this.comentarioRepository = comentarioRepository;
    }

    public Optional<Comentario> buscarPorTransacaoId(Long transacaoId) {
        return comentarioRepository.findByTransacaoId(transacaoId);
    }

    @Transactional
    public Comentario criar(Transacao transacao, ComentarioForm formulario) {
        Comentario comentario = new Comentario(formulario.getTexto().trim(), transacao);
        try {
            return comentarioRepository.saveAndFlush(comentario);
        } catch (DataIntegrityViolationException ex) {
            throw new ComentarioJaExisteException();
        }
    }

    @Transactional
    public Comentario atualizar(Comentario comentario, String novoTexto) {
        comentario.setTexto(novoTexto.trim());
        return comentarioRepository.saveAndFlush(comentario);
    }

    @Transactional
    public void excluir(Comentario comentario) {
        comentarioRepository.delete(comentario);
    }
}
