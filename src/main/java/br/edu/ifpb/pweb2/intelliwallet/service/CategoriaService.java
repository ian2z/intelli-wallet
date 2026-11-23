package br.edu.ifpb.pweb2.intelliwallet.service;

import org.springframework.dao.DataIntegrityViolationException;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.server.ResponseStatusException;

import br.edu.ifpb.pweb2.intelliwallet.model.Categoria;
import br.edu.ifpb.pweb2.intelliwallet.model.CategoriaForm;
import br.edu.ifpb.pweb2.intelliwallet.repository.CategoriaRepository;

@Service
public class CategoriaService {

    private final CategoriaRepository categoriaRepository;

    public CategoriaService(CategoriaRepository categoriaRepository) {
        this.categoriaRepository = categoriaRepository;
    }

    @Transactional(readOnly = true)
    public Page<Categoria> listarPaginado(Pageable pageable) {
        return categoriaRepository.listarOrdenadas(pageable);
    }

    @Transactional(readOnly = true)
    public Categoria buscarPorId(Long id) {
        return categoriaRepository.findById(id)
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "Categoria não encontrada"));
    }

    @Transactional
    public Categoria criar(CategoriaForm formulario) {
        String nome = formulario.getNome().trim();
        if (categoriaRepository.existsByNomeIgnoreCase(nome)) {
            throw new CategoriaInvalidaException("nome", "Já existe uma categoria com este nome");
        }

        Categoria categoria = new Categoria();
        categoria.setNome(nome);
        categoria.setNatureza(formulario.getNatureza());
        categoria.setOrdem(formulario.getOrdem());
        categoria.setAtivo(formulario.isAtivo());
        try {
            return categoriaRepository.saveAndFlush(categoria);
        } catch (DataIntegrityViolationException ex) {
            throw new CategoriaInvalidaException("nome", "Já existe uma categoria com este nome");
        }
    }

    @Transactional
    public Categoria atualizar(Long id, CategoriaForm formulario) {
        Categoria categoria = buscarPorId(id);
        String nome = formulario.getNome().trim();
        if (categoriaRepository.existsByNomeIgnoreCaseAndIdNot(nome, id)) {
            throw new CategoriaInvalidaException("nome", "Já existe uma categoria com este nome");
        }

        categoria.setNome(nome);
        categoria.setNatureza(formulario.getNatureza());
        categoria.setOrdem(formulario.getOrdem());
        categoria.setAtivo(formulario.isAtivo());
        try {
            return categoriaRepository.saveAndFlush(categoria);
        } catch (DataIntegrityViolationException ex) {
            throw new CategoriaInvalidaException("nome", "Já existe uma categoria com este nome");
        }
    }

    @Transactional
    public Categoria alternarAtivo(Long id) {
        Categoria categoria = buscarPorId(id);
        categoria.setAtivo(!categoria.isAtivo());
        return categoriaRepository.saveAndFlush(categoria);
    }
}
