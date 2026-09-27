package br.edu.ifpb.pweb2.intelliwallet.service;

import java.math.BigDecimal;
import java.time.LocalDate;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.ApplicationArguments;
import org.springframework.boot.ApplicationRunner;
import org.springframework.context.annotation.Profile;
import org.springframework.core.annotation.Order;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;

import br.edu.ifpb.pweb2.intelliwallet.model.Categoria;
import br.edu.ifpb.pweb2.intelliwallet.model.Conta;
import br.edu.ifpb.pweb2.intelliwallet.model.Correntista;
import br.edu.ifpb.pweb2.intelliwallet.model.Movimento;
import br.edu.ifpb.pweb2.intelliwallet.model.Papel;
import br.edu.ifpb.pweb2.intelliwallet.model.TipoConta;
import br.edu.ifpb.pweb2.intelliwallet.model.Transacao;
import br.edu.ifpb.pweb2.intelliwallet.repository.CategoriaRepository;
import br.edu.ifpb.pweb2.intelliwallet.repository.ContaRepository;
import br.edu.ifpb.pweb2.intelliwallet.repository.CorrentistaRepository;
import br.edu.ifpb.pweb2.intelliwallet.repository.TransacaoRepository;

@Component
@Profile("dev")
@Order(2)
public class DadosDemonstracaoSeed implements ApplicationRunner {

    private final CorrentistaRepository correntistaRepository;
    private final ContaRepository contaRepository;
    private final CategoriaRepository categoriaRepository;
    private final TransacaoRepository transacaoRepository;
    private final SenhaService senhaService;
    private final String adminLogin;
    private final String adminPassword;
    private final String demoPassword;

    public DadosDemonstracaoSeed(CorrentistaRepository correntistaRepository,
            ContaRepository contaRepository, CategoriaRepository categoriaRepository,
            TransacaoRepository transacaoRepository, SenhaService senhaService,
            @Value("${app.seed.admin-login}") String adminLogin,
            @Value("${app.seed.admin-password}") String adminPassword,
            @Value("${app.seed.demo-password}") String demoPassword) {
        this.correntistaRepository = correntistaRepository;
        this.contaRepository = contaRepository;
        this.categoriaRepository = categoriaRepository;
        this.transacaoRepository = transacaoRepository;
        this.senhaService = senhaService;
        this.adminLogin = adminLogin;
        this.adminPassword = adminPassword;
        this.demoPassword = demoPassword;
    }

    @Override
    @Transactional
    public void run(ApplicationArguments args) {
        criarCorrentista(adminLogin, "Administrador", adminPassword, Papel.ADMIN);
        Correntista correntista = criarCorrentista("teste", "Correntista de teste", demoPassword,
                Papel.CORRENTISTA);
        Conta corrente = criarConta(correntista, "1001-0", "Conta corrente de teste", TipoConta.CORRENTE, null);
        criarConta(correntista, "4000-2", "Cartão de teste", TipoConta.CARTAO, 15);
        criarTransacao(corrente, "Salário", "Salário de demonstração", Movimento.CREDITO,
                new BigDecimal("3500.00"));
        criarTransacao(corrente, "Feira", "Compra de demonstração", Movimento.DEBITO,
                new BigDecimal("125.90"));
    }

    private Correntista criarCorrentista(String login, String nome, String senha, Papel papel) {
        return correntistaRepository.findByLoginIgnoreCase(login).orElseGet(() -> {
            Correntista correntista = new Correntista();
            correntista.setLogin(login);
            correntista.setNome(nome);
            correntista.setSenha(senhaService.gerarHash(senha));
            correntista.setPapel(papel);
            return correntistaRepository.save(correntista);
        });
    }

    private Conta criarConta(Correntista correntista, String numero, String descricao,
            TipoConta tipo, Integer diaFechamento) {
        return contaRepository.findByCorrentistaIdAndNumero(correntista.getId(), numero).orElseGet(() -> {
            Conta conta = new Conta();
            conta.setCorrentista(correntista);
            conta.setNumero(numero);
            conta.setDescricao(descricao);
            conta.setTipo(tipo);
            conta.setDiaFechamento(diaFechamento);
            return contaRepository.save(conta);
        });
    }

    private void criarTransacao(Conta conta, String nomeCategoria, String descricao,
            Movimento movimento, BigDecimal valor) {
        LocalDate data = LocalDate.of(2026, 9, 1);
        if (transacaoRepository.existsByContaIdAndDescricaoAndData(conta.getId(), descricao, data)) {
            return;
        }
        Categoria categoria = categoriaRepository.findByNome(nomeCategoria).orElseThrow();
        Transacao transacao = new Transacao();
        transacao.setConta(conta);
        transacao.setCategoria(categoria);
        transacao.setDescricao(descricao);
        transacao.setData(data);
        transacao.setMovimento(movimento);
        transacao.setValor(valor);
        transacaoRepository.save(transacao);
    }
}
