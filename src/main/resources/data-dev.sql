-- Dados de exemplo para desenvolver as telas da Etapa I.
-- As senhas de exemplo são admin_dev_123 e teste_dev_123, gravadas como BCrypt.
INSERT INTO correntistas (nome, login, senha_hash, papel, bloqueado) VALUES
('Administrador', 'admin', '$2a$10$0J3218c4eOLwTbzmjp094OqWJqEwLcdvD1rja1CWOKXNpI7uMENYq', 'ADMIN', false),
('Correntista de teste', 'teste', '$2a$10$B12QjytDRIAS29Zupv7X9u4MKDGQKxgCFjwpbgEpCoPcYsudmGdtq', 'CORRENTISTA', false)
ON CONFLICT (login) DO NOTHING;

INSERT INTO contas (numero, descricao, tipo, dia_fechamento, correntista_id)
SELECT dados.numero, dados.descricao, dados.tipo, dados.dia_fechamento, correntista.id
FROM correntistas correntista
CROSS JOIN (VALUES
    ('1001-0', 'Conta corrente de teste', 'CORRENTE', NULL::integer),
    ('4000-2', 'Cartão de teste', 'CARTAO', 15)
) AS dados(numero, descricao, tipo, dia_fechamento)
WHERE correntista.login = 'teste'
ON CONFLICT (correntista_id, numero) DO NOTHING;

INSERT INTO transacoes (data, descricao, valor, movimento, categoria_id, conta_id)
SELECT DATE '2026-09-01', dados.descricao, dados.valor, dados.movimento, categoria.id, conta.id
FROM (VALUES
    ('Salário', 'Salário de demonstração', 3500.00, 'CREDITO'),
    ('Feira', 'Compra de demonstração', 125.90, 'DEBITO')
) AS dados(nome_categoria, descricao, valor, movimento)
JOIN categorias categoria ON categoria.nome = dados.nome_categoria
JOIN correntistas correntista ON correntista.login = 'teste'
JOIN contas conta ON conta.correntista_id = correntista.id AND conta.numero = '1001-0'
WHERE NOT EXISTS (
    SELECT 1 FROM transacoes existente
    WHERE existente.conta_id = conta.id
      AND existente.descricao = dados.descricao
      AND existente.data = DATE '2026-09-01'
);
