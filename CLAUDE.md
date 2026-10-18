# CLAUDE.md — Controle de Kitnets API

> Rascunho. Lido automaticamente pelo Claude Code em toda conversa. Mantenha curto.
> Regras de NEGÓCIO ficam em `docs/dominio.md`. Aqui ficam regras de CÓDIGO.

## Projeto
API REST para controle de kitnets: prédios, apartamentos, inquilinos, valores e lançamentos de aluguel.
Reescrita moderna de `thiago-jv/API-CONTROLE-DE-KITNETS` — o projeto antigo é **referência de domínio, não de código**.

## Antes de qualquer tarefa
1. Leia `docs/dominio.md` na seção da feature.
2. Encontrou `[DECIDIR]` ligado à tarefa? **Pare e pergunte.** Nunca escolha sozinho uma regra de negócio.
3. Algo não está no dominio.md? Pergunte, ou deixe `// TODO(negocio): <pergunta>` e avise.

## Stack (não troque sem pedir)
- Java 25 · Spring Boot 4.1.x (Spring Framework 7, Jakarta EE 11, Hibernate 7, Jackson 3)
- Maven Wrapper · empacotamento jar
- Spring Web, Data JPA, Validation, Security (OAuth2 Resource Server + JWT)
- PostgreSQL 16 + Flyway
- MapStruct (mapeamento) · Lombok só em entidades (`@Getter @Setter`, nunca `@Data`)
- springdoc-openapi (versão compatível com Boot 4, ver pom.xml) · RabbitMQ (spring-boot-starter-amqp)
- Testes: JUnit (gerenciado pelo Boot), AssertJ, Mockito, RestTestClient, Testcontainers 2
- Testcontainers 2: artefatos com prefixo "testcontainers-" (ex.: testcontainers-postgresql)

## Atenção: Spring Boot 4 (NÃO use APIs do Boot 3)
- Starters modulares: spring-boot-starter-webmvc, spring-boot-starter-flyway, spring-boot-starter-webmvc-test
- Jackson 3: pacote tools.jackson, JsonMapper (não ObjectMapper de com.fasterxml)
- Testes: @MockitoBean (não @MockBean)
- Null-safety: JSpecify
- Se não tiver certeza de uma API do Boot 4, diga isso e consulte a documentação. Não invente.

## Pacotes
```
com.kitnets
├── api
│   ├── exceptionhandler     # ApiExceptionHandler → ProblemDetail
│   └── v1
│       ├── controller
│       ├── openapi          # *ControllerOpenApi (anotações springdoc)
│       ├── model            # *Model  (records de saída)
│       ├── input            # *Input  (records de entrada, com Bean Validation)
│       └── mapper           # MapStruct
├── core                     # config: security, jackson, openapi, rabbitmq
├── domain
│   ├── model                # entidades + enums de status + regras de negócio
│   ├── repository
│   ├── service              # Cadastro*Service, FluxoLancamentoService
│   ├── exception
│   ├── event
│   └── filter
└── infrastructure           # ViaCEP client, specs, listener → RabbitMQ, relatório PDF
```
`domain` nunca importa `api` nem `infrastructure`.

## Regras de código
- Java/Boot moderno: `jakarta.*`; injeção por construtor; DTOs como `record`; `RestClient` (não RestTemplate);
  `SecurityFilterChain` como @Bean; `ProblemDetail` nativo; springdoc (não SpringFox).
- Controller só faz HTTP. Nunca devolve entidade. Nunca chama repository.
- Regra de negócio na entidade (`lancamento.calcularValores()`, `apartamento.ocupar()`), orquestração no service.
- `@Transactional` em todo método de service que escreve.
- Status sempre `enum`, nunca `String`.
- Dinheiro: `BigDecimal` com escala definida. Datas: `LocalDate` / `OffsetDateTime`, JSON em ISO-8601.
- `@ManyToOne` sempre `LAZY`; listagens com `JOIN FETCH` ou `@EntityGraph`. `open-in-view=false`.
- Exclusão bloqueada por FK → `EntidadeEmUsoException` (409). Não encontrado → `*NaoEncontradoException` (404).
  Regra violada → `NegocioException` (400).
- Evento → RabbitMQ só após commit (`@TransactionalEventListener(phase = AFTER_COMMIT)`).
- Segredos via variável de ambiente. Nada de senha no `application.yml` versionado.
- Nunca crie bean de ObjectMapper/JsonMapper, nem em src/main nem em src/test. Use o auto-configurado pelo Boot.
- Validações de regra de negócio (ex.: duplicidade) ficam no service, nunca no controller.
- Ao corrigir algo que pedi, não contorne movendo o problema para outro lugar; se não for possível, pare e explique.

## Banco
- Migrations em `src/main/resources/db/migration`, `V<n>__<descricao>.sql`. **Nunca edite migration existente.**
- `spring.jpa.hibernate.ddl-auto=validate`. Dados de exemplo em `db/testdata` (só profile `dev`).
- Tabelas e colunas em snake_case minúsculo (`controle_lancamento`, `data_entrada`).

## Comandos
- Subir Postgres + RabbitMQ: `docker compose up -d`
- Rodar: `./mvnw spring-boot:run -Dspring-boot.run.profiles=dev`
- Testes + build: `./mvnw verify`
- Swagger: http://localhost:8080/swagger-ui.html

## Como trabalhar comigo
- Uma feature por vez. Plano antes de código. Diffs pequenos (até ~8 arquivos por etapa).
- Todo endpoint novo tem teste de API: caminho feliz + ao menos 1 erro.
- Regras de cálculo do lançamento têm teste de unidade usando os exemplos do `dominio.md`.
- Ao terminar: rode `./mvnw verify`, mostre o resultado e resuma em até 5 linhas o que mudou.
- Não adicione dependência no `pom.xml` sem justificar.