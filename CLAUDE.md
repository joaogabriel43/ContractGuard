# ContractGuard — Arquitetura, Regras e Decisões

> **Este arquivo é a fonte de verdade para todas as interações com agentes de IA neste repositório.**
> Toda sessão de desenvolvimento deve começar pela leitura deste arquivo.

---

## Status do Projeto

**✅ DEPLOYED — Production ready** (2026-04-26)

| Ambiente | URL |
|---|---|
| Backend (Railway) | https://humorous-joy-production.up.railway.app |
| Frontend (Vercel) | https://contract-guard-cyan.vercel.app |

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

### ADR-002: Armazenamento de Specs como TEXT no PostgreSQL

- **Status**: Revisada (2026-04-26) — decisão original substituída
- **Contexto**: Precisamos armazenar múltiplas versões de specs OpenAPI (que são documentos JSON/YAML de
  estrutura potencialmente variável). As opções consideradas foram: (1) armazenar como texto (`TEXT`), (2)
  armazenar como `JSONB` no PostgreSQL, (3) usar um object store externo (S3, MinIO).
- **Decisão original**: Armazenar como `JSONB`. **Decisão atual**: Armazenar como `TEXT`.
- **Por que a decisão foi revisada**: A coluna `JSONB` exige que o conteúdo seja JSON válido. Specs OpenAPI
  enviadas em formato YAML causavam `DataAccessException` na inserção, pois YAML não é JSON válido. A
  conversão de YAML → JSON antes de persistir introduzia dependência de `swagger-core`'s `Json.mapper()`,
  que registra `SwaggerAnnotationIntrospector` globalmente e causa `NoClassDefFoundError` por exigir JAXB
  (removido no Java 11+). A solução mais simples e robusta foi mudar para `TEXT` (migration V3).
- **Justificativa atual**:
  - `TEXT` aceita JSON e YAML sem conversão — o Swagger Parser lida com ambos nativamente.
  - Elimina a dependência de JAXB e a complexidade de conversão na camada de persistência.
  - A vantagem de queries JSONB (operadores `->`, `->>`) não é necessária no modelo atual — o conteúdo
    é sempre lido inteiro para parsing pelo `SwaggerParserContractAnalyzer`.
- **Consequências**: Perda da indexação GIN e queries estruturais sobre o conteúdo da spec. Aceito para
  o escopo atual; pode ser reintroduzido futuramente se houver necessidade de buscas dentro da spec.

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

---

### [2026-04-25] Erro: `out/` não-ancorado no `.gitignore` excluiu `domain/port/out` do repositório (causa raiz real)

**O que aconteceu**: Os Output Ports da arquitetura hexagonal **nunca foram
commitados**. O padrão `out/` sem `/` no `.gitignore` (seção IntelliJ IDEA)
excluía recursivamente qualquer diretório `out/` na árvore, incluindo
`domain/port/out/`. O build local funcionava porque os arquivos existiam no
filesystem. O Railway falhava porque o repositório nunca os recebeu — o Maven
não encontrava `ServiceRepositoryPort`, `ApiSpecificationRepositoryPort`, etc.
Toda investigação de `.dockerignore`, BuildKit e flags Maven era ruído em cima
de um problema mais simples.

**Como detectar**: Ao suspeitar que arquivos estão faltando no remote, rodar:
```bash
git ls-files src/main/java/br/com/contractguard/domain/port/out/
# saída vazia = arquivos nunca foram commitados
```
`git status` não mostra arquivos ignorados — `git ls-files` é o diagnóstico correto.

**Como prevenir**: Ancorar **todos** os padrões de diretórios de IDE e build
com `/` no `.gitignore`:
```
# ✅ Correto — exclui apenas o /out da raiz (saída do IntelliJ)
/out

# ❌ Errado — exclui domain/port/out/, infrastructure/out/, etc.
out/
```
Após criar ou modificar `.gitignore`, verificar explicitamente que os arquivos
de código-fonte aparecem em `git ls-files` antes do primeiro push.

---

### [2026-04-26] Erro: `@Component` faltando nas implementações de `DiffRule`

**O que aconteceu**: `EndpointRemovedRule` e `RequiredParameterAddedRule` não tinham `@Component`. Spring
injetava uma `List<DiffRule>` vazia em `SwaggerParserContractAnalyzer`. O loop de regras nunca executava —
o diff sempre retornava 0 violations, mesmo com breaking changes óbvias (endpoint removido, parâmetro
obrigatório adicionado).

**Por que era difícil detectar**: Testes unitários instanciavam as regras diretamente (`new EndpointRemovedRule()`),
passando em todos os cenários. O bug só se manifestava com o contexto Spring completo (E2E / produção).

**Como prevenir**: Toda classe que implementa uma interface injetada via `List<T>` **deve** ter `@Component`
(ou outra stereotype annotation). Ao criar uma nova `DiffRule`, o checklist é:
1. Implementar `DiffRule`.
2. Anotar com `@Component`.
3. Adicionar um teste de integração que valide o tamanho de `List<DiffRule>` injetada no contexto Spring.

---

### [2026-04-26] Erro: `LazyInitializationException` ao acessar `violations` fora da sessão JPA

**O que aconteceu**: `GET /api/v1/services/{slug}/reports/latest` retornava 500 com
`LazyInitializationException: failed to lazily initialize a collection of role: DiffReportJpaEntity.violations`.
O use case `GetLatestReportUseCaseImpl` não era `@Transactional`; a sessão Hibernate fechava após o SELECT
do relatório. Quando `mapToDomain()` tentava acessar `entity.getViolations()`, a entidade já estava detached.

**Como prevenir**: Para coleções `@OneToMany` acessadas em um único ponto (query específica), usar
`@EntityGraph` no método de repositório para forçar LEFT JOIN FETCH. Não alterar o `FetchType` global.

```java
// ✅ Correto — eager apenas nesta query
@EntityGraph(attributePaths = {"violations"})
Optional<DiffReportJpaEntity> findFirstByServiceIdOrderByCreatedAtDesc(UUID serviceId);

// ❌ Errado — torna EAGER globalmente, causa N+1 em outras queries
@OneToMany(fetch = FetchType.EAGER)
```

---

### [2026-04-26] Erro: Coluna `raw_content JSONB` rejeitava specs em formato YAML

**O que aconteceu**: `POST /api/v1/services/{slug}/specs` retornava 500 quando o corpo era YAML válido.
PostgreSQL rejeitava a inserção porque `JSONB` exige JSON válido — YAML não é JSON. O Hibernate 6.4 com
`@JdbcTypeCode(SqlTypes.JSON)` em `String` passa o conteúdo bruto ao JDBC sem conversão.

**Como prevenir**: Usar `TEXT` para conteúdo arbitrário (JSON ou YAML). Usar `JSONB` apenas quando o
conteúdo é garantidamente JSON e há necessidade de queries estruturais com `->` / `@>`. Se a conversão
YAML → JSON for necessária, fazê-la **antes** de chegar à camada de persistência, sem depender de
`io.swagger.v3.core.util.Json.mapper()` (que exige JAXB, removido no Java 11+).

---

### [2026-04-26] Erro: `DATABASE_URL` do Railway sem prefixo `jdbc:` quebrava a datasource

**O que aconteceu**: O Railway injeta `DATABASE_URL` no formato `postgresql://user:pass@host/db`. O Spring
Boot espera `jdbc:postgresql://user:pass@host/db`. Usar `DATABASE_URL` diretamente como
`SPRING_DATASOURCE_URL` causava falha de conexão no startup.

**Como prevenir**: Nunca usar `DATABASE_URL` diretamente como `SPRING_DATASOURCE_URL`. Sempre configurar
a variável de ambiente manualmente no Railway com o prefixo `jdbc:`:

```
SPRING_DATASOURCE_URL=jdbc:postgresql://host:port/dbname
SPRING_DATASOURCE_USERNAME=...
SPRING_DATASOURCE_PASSWORD=...
```

Usar as variáveis individuais que o Railway expõe (`PGHOST`, `PGPORT`, `PGDATABASE`, `PGUSER`, `PGPASSWORD`)
para montar a URL manualmente se necessário.

---

## 7. Configurações de Ambiente de Produção

### Railway (Backend)

| Variável | Valor esperado | Observação |
|---|---|---|
| `SPRING_DATASOURCE_URL` | `jdbc:postgresql://host:port/dbname` | Nunca usar `DATABASE_URL` diretamente — falta o prefixo `jdbc:` |
| `SPRING_DATASOURCE_USERNAME` | valor de `PGUSER` | |
| `SPRING_DATASOURCE_PASSWORD` | valor de `PGPASSWORD` | |
| `CORS_ALLOWED_ORIGINS` | `https://contract-guard-cyan.vercel.app` | Sem barra final — Spring rejeita URL com trailing slash |

### Vercel (Frontend Angular)

| Configuração | Valor |
|---|---|
| Root Directory | `frontend` |
| Output Directory | `dist/frontend` |
| Build Command | `ng build --configuration production` |

O arquivo `vercel.json` na raiz do repositório deve conter rewrite de SPA para que rotas Angular
funcionem em refresh direto:

```json
{
  "rewrites": [
    { "source": "/(.*)", "destination": "/index.html" }
  ]
}
```

Sem esse rewrite, qualquer URL diferente de `/` retorna 404 no Vercel.
