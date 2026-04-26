# ContractGuard — Arquitetura, Regras e Decisões

> **Este arquivo é a fonte de verdade para todas as interações com agentes de IA neste repositório.**
> Toda sessão de desenvolvimento deve começar pela leitura deste arquivo.

---

## 1. Objetivo do Projeto

**ContractGuard** é um serviço backend que detecta automaticamente **breaking changes em contratos OpenAPI (Swagger)**,
projetado para ser integrado em pipelines de CI/CD (GitHub Actions). Seu propósito é garantir que novas versões de
uma API não quebrem silenciosamente os seus consumidores, alertando antes do merge.

### Proposta de Valor

- **Produtores de API** recebem feedback imediato sobre quebras de contrato diretamente no Pull Request.
- **Consumidores de API** têm garantia automatizada de compatibilidade retroativa.
- **Times de plataforma** ganham rastreabilidade histórica de todas as mudanças de spec em um único lugar.

### Casos de Uso Centrais

1. **Registrar uma spec OpenAPI** (versionada) para um serviço.
2. **Comparar duas versões** de uma spec e catalogar todas as diferenças detectadas.
3. **Classificar cada diferença** como `BREAKING` ou `NON_BREAKING`.
4. **Reportar o resultado** via webhook, API REST ou saída padrão para o CI/CD.

---

## 2. Arquitetura (Clean Architecture + DDD)

### Princípio Fundamental

> **A regra de dependência:** código-fonte sempre aponta **de fora para dentro**. Nenhuma camada interna
> conhece nada sobre camadas externas.

```
┌──────────────────────────────────────────────────────────────┐
│                        presentation                          │  ← REST controllers, mappers
│  ┌────────────────────────────────────────────────────────┐  │
│  │                      application                       │  │  ← Use cases, Commands/Queries
│  │  ┌──────────────────────────────────────────────────┐  │  │
│  │  │                    domain                        │  │  │  ← Aggregates, VOs, Domain Services
│  │  │          (zero external dependencies)            │  │  │
│  │  └──────────────────────────────────────────────────┘  │  │
│  └────────────────────────────────────────────────────────┘  │
│                      infrastructure                          │  ← JPA, Flyway, Swagger Parser adapter
└──────────────────────────────────────────────────────────────┘
```

### Mapeamento de Pacotes

```
br.com.contractguard/
├── domain/
│   ├── model/           # Aggregates, Entities, Value Objects (POJOs puros, zero annotations de infra)
│   ├── service/         # Domain Services (lógica que não pertence a um único Aggregate)
│   ├── port/
│   │   ├── in/          # Input Ports (interfaces que os Use Cases implementam — driven)
│   │   └── out/         # Output Ports (interfaces de repositório/gateway — driving)
│   └── exception/       # Domain exceptions
│
├── application/
│   ├── usecase/         # Implementações dos Use Cases (interactors)
│   ├── command/         # Command objects (write side)
│   ├── query/           # Query objects (read side)
│   └── dto/             # DTOs internos de aplicação (não expostos na presentation)
│
├── infrastructure/
│   ├── persistence/
│   │   ├── entity/      # JPA @Entity classes (nunca entram no domain)
│   │   ├── repository/  # Spring Data JPA interfaces
│   │   └── adapter/     # Implementações dos Output Ports (domain/port/out)
│   ├── diff/            # Adapter do motor de diff (Swagger Parser)
│   └── config/          # Beans de configuração Spring (@Configuration)
│
└── presentation/
    ├── controller/      # @RestController classes
    ├── request/         # Request DTOs (entrada HTTP)
    ├── response/        # Response DTOs (saída HTTP)
    └── mapper/          # MapStruct mappers (request/response ↔ domain/application DTOs)
```

### Regras de Dependência por Camada

| Camada | Pode depender de | NÃO pode depender de |
|---|---|---|
| `domain` | Nada externo. Apenas Java puro + Lombok. | Spring, JPA, qualquer framework |
| `application` | `domain` | `infrastructure`, `presentation` |
| `infrastructure` | `domain`, `application`, Spring, JPA | `presentation` |
| `presentation` | `application`, `domain` (VOs/exceptions) | `infrastructure` diretamente |

---

## 3. Convenções de Código e Testes

### 3.1 Estilo de Código

- **Java 17**: Usar records, sealed classes e pattern matching onde aplicável.
- **Lombok**: Permitido em todas as camadas **exceto** classes de domínio puras (prefira records).
- **MapStruct**: Obrigatório para mapeamentos entre camadas. Zero mapeamentos manuais em controllers.
- **Nenhum `var` ambíguo**: `var` é permitido apenas quando o tipo é óbvio pelo lado direito da expressão.
- **Imutabilidade**: Objetos de domínio devem ser imutáveis por padrão (records ou `@Value` do Lombok).
- **Exceções**: Domínio lança exceções específicas (`DomainException` e subclasses). Nunca `RuntimeException` genérica.

### 3.2 TDD — Mandatório

O ciclo **Red → Green → Refactor** é a única forma de escrever código novo neste projeto:

1. **Red**: Escreva o teste que falha primeiro.
2. **Green**: Escreva o mínimo de código para o teste passar.
3. **Refactor**: Refatore mantendo todos os testes verdes.

**Não será aceito código de produção sem teste correspondente.**

### 3.3 Camadas de Teste

| Tipo | Ferramenta | Escopo |
|---|---|---|
| **Unitário** | JUnit 5 + Mockito | `domain/`, `application/` |
| **Integração** (slice) | `@DataJpaTest` + Testcontainers PostgreSQL | `infrastructure/persistence/` |
| **Integração** (full) | `@SpringBootTest` + Testcontainers | Fluxos end-to-end |
| **Contract** | Futura expansão com Pact | — |

### 3.4 Nomenclatura de Testes

```java
// Padrão: should_[resultado esperado]_when_[condição]
@Test
void should_classify_as_breaking_when_required_field_is_removed() { }

// Ou BDD com @DisplayName
@DisplayName("GIVEN a spec with a removed required field WHEN diff is computed THEN result is BREAKING")
```

### 3.5 Cobertura Mínima

- `domain/`: **100%** (sem exceção).
- `application/`: **≥ 90%**.
- `infrastructure/`: **≥ 70%** (foco em adapters, não em config).

### 3.6 Proibições

- `System.out.println` → use `@Slf4j` + SLF4J.
- Lógica de negócio em `@RestController`.
- Anotações JPA (`@Entity`, `@Column`, etc.) em classes do pacote `domain/`.
- `Optional.get()` sem `isPresent()` — prefira `orElseThrow()`.

---

## 4. Architecture Decision Records (ADRs)

### ADR-001: Análise Estática vs. Consumer-Driven Contracts (Pact)

- **Status**: Aceita
- **Contexto**: Para detectar breaking changes em APIs, existem duas abordagens principais: (1) **Análise
  Estática** — comparar a spec OpenAPI diretamente, sem executar o serviço; (2) **Consumer-Driven Contracts
  (CDC)** — consumidores definem contratos (ex: Pact) e o produtor os valida em tempo de teste.
- **Decisão**: Adotar **Análise Estática** como motor primário do ContractGuard.
- **Justificativa**:
  - CDC exige que todos os consumidores adotem Pact e mantenham seus contratos atualizados — alto custo de adoção.
  - A maioria das APIs B2B/internas já possui specs OpenAPI no repositório, tornando a análise estática de
    adoção zero-friction.
  - Análise estática detecta breaking changes **antes do deploy**, enquanto CDC requer execução de testes.
  - O custo de falsos negativos (mudanças não detectadas) é reduzido combinando análise estática com semver.
- **Consequências**: Dependemos da qualidade e atualização das specs OpenAPI pelos times produtores. Specs
  desatualizadas geram falsos negativos. Mitigação: validação de spec ao registrar.

---

### ADR-002: Armazenamento de Specs como JSONB no PostgreSQL

- **Status**: Aceita
- **Contexto**: Precisamos armazenar múltiplas versões de specs OpenAPI (que são documentos JSON/YAML de
  estrutura potencialmente variável). As opções consideradas foram: (1) armazenar como texto (`TEXT`), (2)
  armazenar como `JSONB` no PostgreSQL, (3) usar um object store externo (S3, MinIO).
- **Decisão**: Armazenar o conteúdo da spec como **`JSONB`** em uma coluna do PostgreSQL.
- **Justificativa**:
  - **Simplicidade de infraestrutura**: elimina dependência de um object store externo, mantendo o stack
    restrito a PostgreSQL — que já é obrigatório para os metadados.
  - **Consultas nativas**: `JSONB` permite queries com operadores `->`, `->>`, `@>` diretamente, útil para
    extrair paths específicos da spec para relatórios (ex: listar todos os endpoints de uma versão).
  - **Indexação GIN**: suporte a índices GIN no PostgreSQL permite buscas eficientes dentro das specs.
  - **Compactação automática**: PostgreSQL comprime internamente valores JSONB grandes (TOAST).
  - Specs OpenAPI raramente ultrapassam 500 KB, dentro dos limites confortáveis do TOAST.
- **Consequências**: Acoplamento ao PostgreSQL (sem suporte a MySQL/H2 sem adaptação). Aceito, pois o
  PostgreSQL é a única base suportada por decisão de produto.

---

### ADR-003: Motor de Diff Customizado via Swagger Parser

- **Status**: Aceita
- **Contexto**: Existem bibliotecas prontas para diff de OpenAPI (ex: `openapi-diff` da OpenAPITools). A
  escolha é entre adotá-las diretamente ou construir um motor interno usando o **Swagger Parser** como base de
  parsing.
- **Decisão**: Construir um **motor de diff customizado** (`DiffEngine`) que usa o `swagger-parser` para
  fazer parsing e deserialização das specs, mas implementa a lógica de comparação internamente.
- **Justificativa**:
  - **Controle total sobre a classificação**: as regras de `BREAKING` vs. `NON_BREAKING` são o **core de
    negócio** do ContractGuard. Depender de uma lib externa para isso inverte a dependência de domínio.
  - **Extensibilidade**: poder adicionar novas regras de breaking change (ex: mudanças em segurança,
    extensions `x-*` customizadas) sem depender de PRs/releases de terceiros.
  - **Testabilidade**: um motor interno é 100% testável em isolamento com JUnit, sem mocks complexos de libs
    externas.
  - **Maturidade do `swagger-parser`**: é a biblioteca de referência da SmartBear (criadora do OpenAPI),
    mantida ativamente e com suporte completo a OpenAPI 3.x e Swagger 2.x.
  - `openapi-diff` possui casos edge não tratados e classificações de breaking change que divergem do que o
    mercado considera breaking (ex: adição de campos opcionais classificada como breaking em alguns cenários).
- **Consequências**: Maior esforço inicial para implementar o motor de comparação (cobertura de todos os
  objetos do schema OpenAPI). Mitigação: abordagem iterativa, começando pelos breaking changes mais críticos
  (remoção de endpoints, mudança de tipo, remoção de campos obrigatórios) e expandindo por sprints.

---

## 5. Regras de Git e Commits

### 5.1 Conventional Commits

Todos os commits **devem** seguir o padrão [Conventional Commits](https://www.conventionalcommits.org/):

```
<tipo>[escopo opcional]: <descrição curta em imperativo>

[corpo opcional]

[rodapé opcional]
```

#### Tipos permitidos

| Tipo | Quando usar |
|---|---|
| `feat` | Nova funcionalidade (gera MINOR no semver) |
| `fix` | Correção de bug (gera PATCH no semver) |
| `chore` | Tarefas de manutenção, setup, dependências |
| `docs` | Apenas documentação |
| `refactor` | Refatoração sem mudança de comportamento |
| `test` | Adição ou correção de testes |
| `perf` | Melhoria de performance |
| `ci` | Mudanças em pipeline CI/CD |
| `build` | Mudanças no sistema de build (pom.xml, Gradle) |

#### Breaking Change

Adicionar `!` após o tipo ou `BREAKING CHANGE:` no rodapé:

```
feat(api)!: remove deprecated /v1/specs endpoint

BREAKING CHANGE: O endpoint /v1/specs foi removido. Use /v2/contracts.
```

### 5.2 Nomenclatura de Branches

```
feat/<issue-id>-descricao-curta
fix/<issue-id>-descricao-curta
chore/<descricao-curta>
release/<versao>
```

Exemplos:
```
feat/12-diff-engine-endpoint-removal
fix/34-swagger-parser-null-schema
chore/setup-testcontainers
```

### 5.3 Regras de Pull Request

- **Título do PR**: mesmo formato de Conventional Commit.
- **Sem merge direto na `main`** — toda mudança passa por PR.
- **Aprovação mínima**: 1 reviewer humano (quando em time).
- **CI obrigatório**: build + testes devem estar verdes antes do merge.
- **Squash merge** preferido para manter histórico linear (exceto branches de release).

### 5.4 Versionamento (SemVer)

```
MAJOR.MINOR.PATCH[-SNAPSHOT]
```

- `MAJOR`: breaking change na API do ContractGuard (não na API analisada).
- `MINOR`: nova funcionalidade retrocompatível.
- `PATCH`: bug fix.
- Desenvolvimento ativo: sufixo `-SNAPSHOT`.

---

## 6. Erros Conhecidos e Como Evitá-los

### [2026-04-25] Erro: `.dockerignore` sem ancoragem excluiu `domain/port/out` do build context

**O que aconteceu**: Build Docker no Railway falhava com
`package br.com.contractguard.domain.port.out does not exist` e
`cannot find symbol: class ServiceRepositoryPort`. Os arquivos existiam
localmente e o pacote estava correto, mas o build funciona localmente e
falha apenas no container.

**Por que**: Padrões sem prefixo `/` no `.dockerignore` (mesmo algoritmo do
`.gitignore`) são **recursivos** — casam com qualquer diretório de mesmo nome
em qualquer nível da árvore. O padrão `out` excluía silenciosamente
`src/main/java/br/com/contractguard/domain/port/out/` do contexto de build
enviado ao Docker daemon. Para o Maven dentro do container, os arquivos
simplesmente não existiam.

**Como prevenir**: Todo padrão de diretório de build/output no `.dockerignore`
deve ser prefixado com `/` para ancorar à raiz do repositório:

```
# ✅ Correto — exclui apenas o diretório raiz
/target
/out
/build
/dist

# ❌ Errado — exclui recursivamente qualquer dir com esse nome
target
out
build
dist
```

**Risco específico deste projeto**: A Arquitetura Hexagonal usa `domain/port/in`
e `domain/port/out`. Padrões `in` e `out` sem ancoragem excluiriam os
Input/Output Ports inteiros do build, causando erros de compilação difíceis
de diagnosticar (o código existe localmente, o erro só aparece no CI/CD).
