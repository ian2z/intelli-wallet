# IntelliWallet

Aplicação web de carteira inteligente e controle financeiro, desenvolvida para a disciplina de **Programação Web 2 (PWEB2)** no **Instituto Federal da Paraíba (IFPB)**.

---

## Tecnologias Utilizadas

- **Linguagem:** Java 17
- **Framework:** Spring Boot 4.x
  - Spring Data JPA (Persistência)
  - Spring Starter Thymeleaf (Camada de visualização/templates)
  - Spring Starter Web Services
  - Spring Boot DevTools
  - Spring Boot Docker Compose (Suporte automático a containers)
- **Banco de Dados:** PostgreSQL 
- **Gerenciador de Dependências:** Apache Maven
- **Containerização:** Docker / Docker Compose

---

## Como Executar o Projeto

### Pré-requisitos
- **Java JDK 17** instalado
- **Docker** e **Docker Compose** instalados e em execução
- **Git** instalado

### Passo a Passo

1. **Clonar o repositório:**
   ```bash
   git clone git@github.com:ian2z/intelli-wallet.git
   cd intelli-wallet
   ```

2. **Inicializar o banco de dados (PostgreSQL):**
   O ambiente local usa PostgreSQL 17. As configurações padrão de desenvolvimento são banco e usuário `intelliwallet`, senha `intelliwallet_dev` e porta `5432`. Para alterar esses valores, defina `INTELLI_DB_NAME`, `INTELLI_DB_USER`, `INTELLI_DB_PASSWORD` e `INTELLI_DB_PORT` em um arquivo `.env` local (ignorado pelo Git) e use os mesmos valores no ambiente da aplicação. A senha padrão serve apenas para desenvolvimento. O Docker Compose é iniciado manualmente para que os comandos Maven não dependam do daemon Docker.
   ```bash
   docker compose up -d
   docker compose ps
   ```

3. **Compilar e rodar a aplicação:**
   - **Linux / macOS:**
     ```bash
     ./mvnw spring-boot:run
     ```
   - **Windows:**
     ```cmd
     mvnw.cmd spring-boot:run
     ```

4. **Acessar a aplicação:**
   - Abra o navegador em: [http://localhost:8080](http://localhost:8080)

O arquivo `src/main/resources/data.sql` insere as 22 categorias predefinidas. No perfil `dev` (padrão local), `data-dev.sql` adiciona o administrador `admin` e o correntista `teste`, com duas contas e transações de exemplo. As senhas de exemplo são `admin_dev_123` e `teste_dev_123`; os valores gravados são hashes BCrypt. Use outro perfil e outro banco fora do desenvolvimento para não carregar os dados de demonstração. Os scripts podem ser executados novamente sem duplicar registros.

As telas usam Tailwind CSS compilado em `src/main/resources/static/css/app.css`. O CSS gerado acompanha o repositório. Ao alterar classes nos templates, execute `npm install` e `npm run build:css` para atualizá-lo.

Na Etapa I, antes da autenticação, a página `/contas` permite selecionar um correntista para demonstrar o UC02. A tabela consulta somente as contas da pessoa escolhida. A criação de contas correntes e cartões pelo próprio correntista não faz parte dos casos de uso pontuados desta etapa e ainda depende de confirmação com o professor.

---

## Convenção de Branches

Adotamos um modelo baseado em **Gitflow Simplificado**, separando o código estável, o código em desenvolvimento e as branches de trabalho individuais.

### 1. Branches Principais (Permanentes)

| Branch | Descrição |
|---|---|
| `main` | Código de produção / versão estável. Reflete o estado das entregas finais da disciplina. Apenas recebe merges via Pull Request aprovado de `develop` (para releases) ou de `hotfix/*`. |
| `develop` | Branch base para todo o desenvolvimento contínuo. Integra todas as novas funcionalidades antes de irem para produção. |

---

### 2. Branches de Trabalho (Temporárias / Short-lived)

Toda nova implementação ou correção deve ser criada em uma branch separada e excluída após o merge.

**Padrão de nomenclatura:**
```
<tipo>/<descricao-curta-em-kebab-case>
```

| Tipo | Descrição | Origem (Branch Base) | Destino (Merge) | Exemplo |
|---|---|---|---|---|
| `feat/` | Nova funcionalidade ou história de usuário | `develop` | `develop` | `feat/cadastro-usuario`, `feat/extrato-mensal` |
| `fix/` | Correção de bug encontrado em ambiente de desenvolvimento | `develop` | `develop` | `fix/validacao-data-lancamento` |
| `hotfix/` | Correção crítica e urgente direto em produção | `main` | `main` e `develop` | `hotfix/erro-conexao-banco` |
| `refactor/` | Refatoração de código sem alteração funcional | `develop` | `develop` | `refactor/servico-transacoes` |
| `docs/` | Criação ou alteração de documentações | `develop` ou `main` | `develop` ou `main` | `docs/atualizar-instrucoes-execucao` |
| `test/` | Adição ou alteração de testes automatizados | `develop` | `develop` | `test/testes-unitarios-carteira` |
| `chore/` | Ajustes de build, dependências ou configurações | `develop` | `develop` | `chore/atualizar-dependencia-postgres` |

---

## Convenção de Commits

Seguimos a especificação do **[Conventional Commits](https://www.conventionalcommits.org/)**. Os commits devem ser informativos, concisos e padronizados.

### Formato da Mensagem:
```
<tipo>(<escopo opcional>): <descrição>
```

### Tipos Aceitos:

- `feat`: Adiciona uma nova funcionalidade ao projeto.
- `fix`: Corrige um bug ou comportamento inesperado.
- `docs`: Alterações exclusivamente na documentação (README, manuais, diagramas).
- `style`: Alterações de formatação (espaçamento, identação, imports), sem impacto na lógica do código.
- `refactor`: Refatoração de código de produção que não altera funcionalidade nem conserta bugs.
- `test`: Adição, ajuste ou correção de testes automatizados.
- `chore`: Modificações em scripts de build, dependências de pacotes, configurações (`pom.xml`, `.gitignore`, `docker-compose`).
- `perf`: Modificação que melhora o desempenho da aplicação.

### Boas Práticas para Mensagens de Commit:
- Escreva a descrição em letras minúsculas (salvo siglas ou nomes de classes).
- Use linguagem direta (ex.: `feat: adicionar tela de cadastro de carteira`).
- Mantenha a primeira linha com no máximo 72 caracteres.
- Não use ponto final ao término da descrição.

### Exemplos Válidos:
```
feat(auth): implementar autenticacao de usuarios
feat(carteira): adicionar endpoint para criacao de nova carteira
fix(saldo): corrigir calculo de saldo com valores decimais
docs: documentar padroes de branch e regras de pr
chore(docker): atualizar versao da imagem do postgresql
test(usuario): criar testes de integracao para login
```

---

## Regras de Pull Request (PR)

Para manter a qualidade e rastreabilidade do código, nenhuma alteração deve ser enviada diretamente para `main` ou `develop` sem Pull Request.

### 1. Fluxo de Trabalho (Workflow)
1. Crie uma branch a partir de `develop`:
   ```bash
   git checkout develop
   git pull origin develop
   git checkout -b feat/minha-nova-funcionalidade
   ```
2. Realize as alterações e faça commits seguindo a convenção.
3. Suba sua branch para o repositório remoto:
   ```bash
   git push -u origin feat/minha-nova-funcionalidade
   ```
4. Abra um **Pull Request** no GitHub direcionando para a branch `develop`.

---

### 2. Checklist do Autor (Antes de Abrir o PR)
- [ ] O projeto compila com sucesso (`./mvnw clean compile`).
- [ ] Todos os testes automatizados foram executados e passaram (`./mvnw test`).
- [ ] Não há arquivos desnecessários no commit (ex: `.idea/`, `.class`, binários ou credenciais).
- [ ] Os commits seguem a convenção do projeto.
- [ ] A branch foi sincronizada com a base mais recente (`git pull origin develop`).

---

### 3. Padrão do Pull Request no GitHub

- **Título do PR:** Deve seguir a convenção de commits.  
  *Exemplo:* `feat(carteira): implementar listagem e filtros de transacoes`
- **Descrição do PR:** Deve conter um resumo claro contendo:
  1. **O que foi feito:** Descrição dos módulos e classes alteradas/criadas.
  2. **Motivação:** Qual problema ou requisito essa alteração atende.
  3. **Como testar:** Passos detalhados ou comandos para reproduzir o teste localmente.
  4. **Evidências (se houver UI):** Prints da tela ou logs de teste.
  5. **Issues vinculadas:** Indique issues relacionadas usando palavras-chave do GitHub (ex: `Closes #12`).

---

### 4. Regras de Revisão e Merge (Code Review)
- **Revisão Obrigatória:** Ao menos 1 membro da equipe deve revisar e aprovar o PR.
- **Discussões:** Qualquer comentário ou apontamento feito na revisão deve ser respondido ou resolvido antes do merge.
- **Estratégia de Merge:**
  - Preferencialmente **Squash and Merge** para branches `feat/` ou `fix/` simples (mantendo o histórico de `develop` limpo).
  - Ou **Create a merge commit** para manter o histórico de branches maiores com histórico detalhado.
- **Exclusão de Branch:** A branch de trabalho deve ser excluída após a conclusão do merge.
