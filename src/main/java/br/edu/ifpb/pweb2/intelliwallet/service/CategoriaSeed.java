package br.edu.ifpb.pweb2.intelliwallet.service;

import java.util.List;

import org.springframework.boot.ApplicationArguments;
import org.springframework.boot.ApplicationRunner;
import org.springframework.core.annotation.Order;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;

import br.edu.ifpb.pweb2.intelliwallet.model.Categoria;
import br.edu.ifpb.pweb2.intelliwallet.model.Natureza;
import br.edu.ifpb.pweb2.intelliwallet.repository.CategoriaRepository;

@Component
@Order(1)
public class CategoriaSeed implements ApplicationRunner {

    private static final List<Definicao> CATEGORIAS = List.of(
            new Definicao("Salário", Natureza.ENTRADA, 1),
            new Definicao("Cashback", Natureza.ENTRADA, 2),
            new Definicao("Resgate Investimento", Natureza.ENTRADA, 3),
            new Definicao("Outras Entradas", Natureza.ENTRADA, 4),
            new Definicao("Saúde e Remédios", Natureza.SAIDA, 1),
            new Definicao("Academia e Personal", Natureza.SAIDA, 2),
            new Definicao("Carros e Uber", Natureza.SAIDA, 3),
            new Definicao("Educação e Cursos", Natureza.SAIDA, 4),
            new Definicao("Lazer e Turismo", Natureza.SAIDA, 5),
            new Definicao("Condomínio", Natureza.SAIDA, 6),
            new Definicao("Energia", Natureza.SAIDA, 7),
            new Definicao("Celular", Natureza.SAIDA, 8),
            new Definicao("Internet", Natureza.SAIDA, 9),
            new Definicao("Itens Pessoais", Natureza.SAIDA, 10),
            new Definicao("Feira", Natureza.SAIDA, 11),
            new Definicao("Casa", Natureza.SAIDA, 12),
            new Definicao("Impostos", Natureza.SAIDA, 13),
            new Definicao("Outros gastos", Natureza.SAIDA, 14),
            new Definicao("Aporte Renda Fixa", Natureza.INVESTIMENTO, 1),
            new Definicao("Aporte Renda Variável", Natureza.INVESTIMENTO, 2),
            new Definicao("Aporte Reserva Emergência", Natureza.INVESTIMENTO, 3),
            new Definicao("Aporte Previdência", Natureza.INVESTIMENTO, 4));

    private final CategoriaRepository categoriaRepository;

    public CategoriaSeed(CategoriaRepository categoriaRepository) {
        this.categoriaRepository = categoriaRepository;
    }

    @Override
    @Transactional
    public void run(ApplicationArguments args) {
        for (Definicao definicao : CATEGORIAS) {
            if (categoriaRepository.findByNome(definicao.nome()).isPresent()) {
                continue;
            }
            Categoria categoria = new Categoria();
            categoria.setNome(definicao.nome());
            categoria.setNatureza(definicao.natureza());
            categoria.setOrdem(definicao.ordem());
            categoriaRepository.save(categoria);
        }
    }

    private record Definicao(String nome, Natureza natureza, int ordem) {
    }
}
