# Domínio — Controle de Kitnets

> **Rascunho.** Fonte da verdade sobre REGRAS DE NEGÓCIO deste sistema.
> A IA consulta este arquivo antes de implementar qualquer feature e **não inventa regra que não esteja aqui**.
>
> Convenções deste arquivo:
> - **Regra** — decidido, implementar assim.
> - **[DECIDIR]** — pergunta em aberto. A IA deve PARAR e perguntar, nunca escolher sozinha.
> - **Sistema antigo:** — como o projeto de referência fazia (só para contexto; não é para copiar).
>
> Referência: `thiago-jv/API-CONTROLE-DE-KITNETS` (Java 11 / Boot 2.7).

---

## 1. Visão geral

Sistema para o dono de kitnets controlar seus **prédios**, **apartamentos** (kitnets), **inquilinos**
e os **lançamentos** de aluguel (quem ocupa qual apartamento, em que período, quanto deve e quanto pagou).
Após cada lançamento, o inquilino é avisado por e-mail. Lançamentos podem ser emitidos em PDF.

**Usuários do sistema:** o proprietário/administrador e, opcionalmente, operadores com permissões limitadas.
Inquilino **não** acessa o sistema no MVP.

## 2. Glossário

| Termo | Significado |
|---|---|
| Prédio | Imóvel com endereço, que contém apartamentos |
| Apartamento / Kitnet | Unidade alugável dentro de um prédio |
| Inquilino | Pessoa que aluga um apartamento |
| Valor | Valor mensal de aluguel cadastrado (tabela de preços) |
| Lançamento | Registro de um período de ocupação: inquilino + apartamento + data de entrada + data de pagamento + valores |
| Data de entrada | Início do período cobrado no lançamento |
| Data de pagamento | Fim do período cobrado / data em que o pagamento vence |
| Diária | Valor mensal ÷ dias do mês |
| Débito | Quanto falta pagar no lançamento |
| Fechar lançamento | Encerrar a ocupação; libera o apartamento |

## 3. Entidades e campos

### 3.1 Prédio
| Campo | Regra |
|---|---|
| descricao | obrigatório, 3–90 caracteres, **único** |
| cep | obrigatório, 8 dígitos (só números) |
| logradouro, complemento, bairro, localidade, uf | preenchidos a partir do CEP, editáveis |
| numero | opcional |

- **Regra:** `GET /v1/predios/cep/{cep}` consulta o ViaCEP e devolve o endereço para o front preencher o formulário.
  CEP inexistente → 404 com mensagem clara. ViaCEP fora do ar → 503 (não 500).
- **Regra:** prédio com apartamentos não pode ser excluído → 409.

### 3.2 Apartamento
| Campo | Regra |
|---|---|
| numero | obrigatório, 1–10 caracteres |
| descricao | obrigatório, 1–90 caracteres |
| medidor | opcional, até 10 caracteres, **único** (número do medidor de luz) |
| status | `DISPONIVEL`, `OCUPADO`, `MANUTENCAO` — padrão `DISPONIVEL` |
| predio | obrigatório |

- **Regra:** o status **não** é alterado pelo PUT de cadastro. Ele muda por regra (lançamento) ou por ação explícita
  (`PUT /v1/apartamentos/{id}/manutencao` e `DELETE /v1/apartamentos/{id}/manutencao`).
- **Regra:** apartamento `OCUPADO` não pode entrar em manutenção.
- **[DECIDIR]** `numero` é único **por prédio** (recomendado) ou único no sistema todo? *Sistema antigo:* único global, assim como `descricao`.
- **Regra:** apartamento com lançamentos não pode ser excluído → 409.

### 3.3 Inquilino
| Campo | Regra |
|---|---|
| nome | obrigatório, até 90 |
| nomeAbreviado | obrigatório, até 20 |
| email | opcional, formato válido — **necessário para receber o aviso de lançamento** |
| contato | opcional, até 15 (telefone) |
| cpf | 11 dígitos, **único**, validar dígito verificador |
| status | `ATIVO`, `INATIVO` — padrão `ATIVO` |

- **Regra:** inquilino `INATIVO` não pode receber novo lançamento.
- **[DECIDIR]** `cpf` é obrigatório? *Sistema antigo:* opcional.
- **[DECIDIR]** Manter o campo `genero`? *Sistema antigo:* obrigatório, padrão MASCULINO — não é usado em nenhuma regra.
- **Regra:** inquilino com lançamentos não é excluído (409); a forma de "remover" é inativar.

### 3.4 Valor (tabela de aluguel)
| Campo | Regra |
|---|---|
| valor | obrigatório, > 0, `BigDecimal` (2 casas), **único** |

- **[DECIDIR]** Valor deveria ser um atributo do **apartamento** (cada kitnet tem seu aluguel) em vez de uma tabela solta?
  Se continuar tabela, adicionar `descricao` (ex.: "Kitnet simples", "Kitnet com garagem").

### 3.5 Diário
- *Sistema antigo:* entidade com apenas `descricao` (até 10 caracteres), CRUD parcial, não usada por nenhuma outra parte.
- **[DECIDIR]** Para que serve? Se ninguém souber, **fica fora do MVP**.

### 3.6 Usuário e permissões
| Campo | Regra |
|---|---|
| nome, email (único, login), senha (BCrypt) | obrigatórios |
| permissões | conjunto de permissões abaixo |

Permissões: `PREDIO`, `APARTAMENTO`, `INQUILINO`, `VALOR`, `LANCAMENTO`, `DIARIO` — combinadas com escopo `READ`/`WRITE` do token.

## 4. Lançamento — o coração do sistema

### 4.1 Dados de ENTRADA (o que o cliente envia)
- inquilinoId, apartamentoId, valorId, dataEntrada, dataPagamento, valorPago, observacao
- status da conta de luz (ver 4.5)

**Regra:** todos os valores calculados (dias, diária, total, débito, status de pagamento) são calculados **pelo servidor**.
*Sistema antigo:* o cliente também enviava os campos calculados no POST.

### 4.2 Cálculos
Sendo `valorMensal` o valor do aluguel (vindo do **Valor** referenciado):

1. `dias = dias entre dataEntrada e dataPagamento`; se as datas forem iguais, `dias = 1`
   - **[DECIDIR]** O dia de entrada conta? Ex.: entrada 01/01, pagamento 31/01 → 30 dias (sistema antigo) ou 31?
2. `valorDiaria = valorMensal ÷ quantidade de dias do mês da dataEntrada`
   - **[DECIDIR]** Arredondamento: sugestão = 2 casas, `HALF_EVEN`, aplicado só no total. *Sistema antigo:* `DECIMAL128` sem escala definida.
   - **[DECIDIR]** Período que atravessa dois meses (ex.: 20/01 a 10/02): usar os dias do mês da entrada (sistema antigo) ou ratear por mês?
3. `valorTotal = dias × valorDiaria`
4. `valorDebito = valorTotal − valorPago` (nunca negativo — **[DECIDIR]** pagamento a maior vira crédito ou é rejeitado?)
5. `statusPagamento = PAGO` se `valorDebito ≤ 0`, senão `DEBITO`

**Exemplo (para teste):** valor 270,00, entrada 01/04, pagamento 11/04 (abril = 30 dias)
→ dias 10 → diária 9,00 → total 90,00 → pago 50,00 → débito 40,00 → `DEBITO`.

**Regra:** os cálculos são métodos da própria entidade (`lancamento.calcularValores()`), não `@Component`s separados.

### 4.3 Validações ao criar
1. `dataPagamento` ≥ `dataEntrada` → senão 400. *(A mensagem do sistema antigo dizia o contrário — corrigir.)*
2. Inquilino deve estar `ATIVO`.
3. Apartamento deve estar `DISPONIVEL`. *Sistema antigo:* não verificava.
4. **Conflito de período:** não pode existir outro lançamento **aberto** cujo período se sobreponha
   (`novaEntrada ≤ existentePagamento` E `novoPagamento ≥ existenteEntrada`) para o **mesmo apartamento** OU para o **mesmo inquilino**.
   *Sistema antigo:* a query tinha `AND/OR` sem parênteses — bloqueava qualquer lançamento de um apartamento que já teve um lançamento, em qualquer data.
5. **[DECIDIR]** Um inquilino pode ter dois apartamentos ao mesmo tempo?

### 4.4 Ciclo de vida
```
         criar                      fechar (só se PAGO)
  ──────────────► ABERTO ──────────────────────────► FECHADO
                    ▲                                   │
                    └──────────── reabrir ──────────────┘
```
| Evento | Lançamento | Apartamento |
|---|---|---|
| Criar | `ABERTO`, statusPagamento calculado | → `OCUPADO` |
| Registrar pagamento / editar | recalcula valores | — |
| Fechar | `FECHADO` — **só se `PAGO`**, senão 400 "apartamento em débito" | → `DISPONIVEL` |
| Reabrir | `ABERTO` — **[DECIDIR]** só se o apartamento ainda estiver `DISPONIVEL`? | → `OCUPADO` |
| Excluir | removido — **[DECIDIR]** só se `ABERTO` e sem pagamento? | → `DISPONIVEL` |

*Sistema antigo:* um único `PUT /controles/{id}/status` que **invertia** true/false. No novo, ações explícitas
(`PUT /lancamentos/{id}/fechamento`, `DELETE /lancamentos/{id}/fechamento`).

**Regra:** criar/fechar/excluir alteram lançamento E apartamento na **mesma transação**.

### 4.5 Conta de luz
*Sistema antigo:* campos `entregaContaLuz` (SIM/NAO), `statusPagamentoLuz` (PAGO/DEBITO) e `statusProximoPagamento` (PAGO/DEBITO),
apenas armazenados, sem nenhuma regra. Havia uma constante `BANDEIRA_AMARELA = 0.960143` sem uso.
- **[DECIDIR]** O que significa cada campo? Existe cálculo de luz (medidor × tarifa)? Se não houver resposta, manter só como campos informativos (booleans/enum) e deixar cálculo de luz fora do MVP.

### 4.6 Aviso por e-mail
- **Regra:** após o lançamento ser criado **e a transação commitada**, publicar evento `LancamentoCriadoEvent`;
  um listener envia a mensagem para a fila `APARTAMENTO_EMAIL` (RabbitMQ).
- Payload: id do lançamento, data de entrada, nome do inquilino, e-mail, valor pago, prédio, número do apartamento, endereço.
- O envio do e-mail em si é responsabilidade de outro serviço (consumidor da fila). Falha na fila **não** desfaz o lançamento.
- Inquilino sem e-mail → não publica.
- *Sistema antigo:* publicava dentro do `salvar`, antes de garantir o commit.

### 4.7 Consultas e relatórios
- Pesquisa paginada de lançamentos com filtros: inquilino, apartamento, prédio, status, período (dataPagamento).
- Estatística: total recebido e total em débito por apartamento num período.
  *Sistema antigo:* `/estatistica/por-apartamento` só filtrava por data e não agrupava por apartamento.
- PDF de um lançamento (comprovante). **[DECIDIR]** manter JasperReports ou gerar de outra forma?

## 5. Endpoints (proposta)

| Recurso | Antigo | Novo |
|---|---|---|
| Prédios | `/v1/predios` + `/cep/{cep}` | igual |
| Apartamentos | `/v1/apartamentos`, `/todos`, `/todos/disponiveis` | `/v1/apartamentos?status=DISPONIVEL` (paginado) + `/{id}/manutencao` |
| Inquilinos | `/v1/inquilinos`, `/todos`, `/todos/ativos` | `/v1/inquilinos?status=ATIVO` |
| Valores | `/v1/valores` | igual (ou dentro de apartamento, ver 3.4) |
| Lançamentos | `/v1/controles` + `PUT /{id}/status` | `/v1/lancamentos` + `/{id}/fechamento` + `/{id}/comprovante` (PDF) |
| Estatísticas | `/v1/controles/estatistica/por-apartamento` | `/v1/estatisticas/recebimentos-por-apartamento` |
| Tokens | `/v1/tokens/revoke` | responsabilidade do Authorization Server |

## 6. Bugs do sistema antigo — NÃO replicar
1. `atualizar` do lançamento recalculava o objeto errado (o do DTO), o salvo nunca era recalculado.
2. `salvar` sem transação: apartamento ficava `OCUPADO` mesmo quando a validação falhava.
3. Query de conflito com `AND/OR` sem parênteses (ver 4.3).
4. Excluir inquilino inexistente lançava `PredioNaoEncontradoException`.
5. `pesquisar` retornava a entidade JPA na API.
6. Status como `String` solta (`"OCUPADO"`), sem enum.
7. Cliente enviava valores calculados.
8. Formato de data `dd/MM/yyyy` global no Jackson (a API deve usar ISO-8601).

## 7. Fora do MVP
- Multi-proprietário / multi-tenant (SaaS)
- Portal do inquilino
- Cálculo de conta de luz
- Pagamento online / boleto / Pix
- Contratos e reajuste anual