# Controle de Kitnets — API

![CI](https://github.com/SamukaJVM/api-kitnet/actions/workflows/ci.yml/badge.svg)

API REST para o dono de kitnets controlar seus **prédios**, **apartamentos**, **inquilinos** e os
**lançamentos** de aluguel (quem ocupa qual apartamento, em que período, quanto deve e quanto
pagou). Após cada lançamento, o inquilino é avisado por e-mail.

Este projeto é uma **reescrita moderna, para fins de estudo**, do projeto
[`thiago-jv/API-CONTROLE-DE-KITNETS`](https://github.com/thiago-jv/API-CONTROLE-DE-KITNETS)
(Java 11 / Spring Boot 2.7). O sistema antigo é usado como **referência de domínio** (as regras
de negócio), não como referência de código — a stack, os padrões e a arquitetura aqui são
atuais.

## Stack

- **Java 25** · **Spring Boot 4.1.1** (Spring Framework 7, Jakarta EE 11, Hibernate 7, Jackson 3)
- Maven Wrapper (`./mvnw`), empacotamento `jar`
- Spring Web, Spring Data JPA, Bean Validation
- **PostgreSQL** (`postgres:18` via Docker Compose) + **Flyway** (`flyway-database-postgresql` 12.4.0)
- **MapStruct** 1.6.3 (mapeamento DTO ↔ entidade) · **Lombok** (`@Getter`/`@Setter` nas entidades)
- **springdoc-openapi** 3.1.1 (Swagger UI)
- Testes: JUnit 5 (gerenciado pelo Spring Boot), AssertJ, `RestTestClient`, **Testcontainers** 2.0.5
  (`testcontainers-postgresql`) para os testes de integração

> Segurança (OAuth2 Resource Server + JWT) está planejada em `CLAUDE.md`, mas ainda não foi
> implementada — não há dependência de Spring Security no `pom.xml` nesta versão.

## Como rodar

### Pré-requisitos
- JDK 25
- Docker (para o Postgres local via `compose.yaml` **e** para os testes de integração, que usam
  Testcontainers)

### Subir o banco local
```bash
docker compose up -d
```
Sobe um Postgres 18 em `localhost:5432` (banco `dbkitnet`, usuário/senha `admin`/`admin`, ver
`compose.yaml`).

### Rodar a aplicação
```bash
./mvnw spring-boot:run -Dspring-boot.run.profiles=dev
```
O profile `dev` ativa SQL de debug e (quando existir) dados de exemplo em `db/testdata`. A API
sobe na porta **8082** (`server.port` em `application.yaml`).

- Swagger UI: http://localhost:8082/swagger-ui.html

### Rodar os testes
```bash
./mvnw verify
```
Os testes de integração usam Testcontainers (sobem um Postgres efêmero próprio, independente do
`compose.yaml`) — só precisam do Docker rodando, não do `docker compose up -d`.

## Regras de negócio

As regras de negócio completas — campos, validações, cálculo do lançamento (mês comercial de 30
dias), ciclo de vida (`ABERTO`/`FECHADO`/reabertura), concorrência e decisões de domínio — estão
documentadas em [`docs/dominio.md`](docs/dominio.md). Resumo:

- **Prédio**: endereço (preenchido via consulta de CEP ao ViaCEP), contém apartamentos.
- **Apartamento**: kitnet dentro de um prédio, com `valorAluguel` próprio e status
  (`DISPONIVEL`/`OCUPADO`/`MANUTENCAO`).
- **Inquilino**: pessoa que aluga um apartamento, com CPF opcional (validado por dígito
  verificador quando informado) e status (`ATIVO`/`INATIVO`).
- **Lançamento**: o coração do sistema — registra um período de ocupação (inquilino +
  apartamento + datas), copia o valor do aluguel do apartamento no momento da criação, calcula
  dias/total/débito pelo mês comercial de 30 dias, e controla o ciclo
  `ABERTO → FECHADO → (reabrir) → ABERTO`.

## Fora do MVP

Decisões conscientes de não implementar nesta versão de estudo (detalhes em `docs/dominio.md`,
seção "Fora do MVP"):
- Multi-proprietário / multi-tenant (SaaS)
- Portal do inquilino
- Cálculo de conta de luz
- Pagamento online / boleto / Pix
- Crédito/saldo quando o pagamento do lançamento é maior que o valor total devido (rejeitado com
  400, nunca gera crédito)
- Edição de data de entrada, data de pagamento, apartamento ou inquilino de um lançamento já
  criado (todos imutáveis; a correção é excluir e criar de novo)
- PDF de comprovante e estatísticas de recebimento (ficam para uma feature futura de "Relatórios")
