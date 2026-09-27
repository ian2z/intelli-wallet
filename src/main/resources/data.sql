-- Categorias obrigatórias do SpendWise. O conflito evita duplicatas ao reiniciar.
UPDATE categorias
SET nome = 'Aporte Reserva Emergencia'
WHERE nome = 'Aporte Reserva Emergência'
  AND NOT EXISTS (SELECT 1 FROM categorias WHERE nome = 'Aporte Reserva Emergencia');

INSERT INTO categorias (nome, natureza, ordem, ativo) VALUES
('Salário', 'ENTRADA', 1, true),
('Cashback', 'ENTRADA', 2, true),
('Resgate Investimento', 'ENTRADA', 3, true),
('Outras Entradas', 'ENTRADA', 4, true),
('Saúde e Remédios', 'SAIDA', 1, true),
('Academia e Personal', 'SAIDA', 2, true),
('Carros e Uber', 'SAIDA', 3, true),
('Educação e Cursos', 'SAIDA', 4, true),
('Lazer e Turismo', 'SAIDA', 5, true),
('Condomínio', 'SAIDA', 6, true),
('Energia', 'SAIDA', 7, true),
('Celular', 'SAIDA', 8, true),
('Internet', 'SAIDA', 9, true),
('Itens Pessoais', 'SAIDA', 10, true),
('Feira', 'SAIDA', 11, true),
('Casa', 'SAIDA', 12, true),
('Impostos', 'SAIDA', 13, true),
('Outros gastos', 'SAIDA', 14, true),
('Aporte Renda Fixa', 'INVESTIMENTO', 1, true),
('Aporte Renda Variável', 'INVESTIMENTO', 2, true),
('Aporte Reserva Emergencia', 'INVESTIMENTO', 3, true),
('Aporte Previdência', 'INVESTIMENTO', 4, true)
ON CONFLICT (nome) DO NOTHING;
