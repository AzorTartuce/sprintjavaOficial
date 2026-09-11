# SprintJava — Documentação Completa do Projeto

> Documento gerado a partir da análise do código-fonte em `src/`. Os arquivos
> citados no `README.md` (`docs/BACKEND_JAVA_COMPLETO.md`,
> `docs/FRONTEND_INTEGRATION.md`, `docs/LOGICA_ANALISE_PYTHON.md`,
> `docs/start.md`, `ESTRUTURA_PROJETO_JAVA.md`) não existem no repositório —
> este documento é a fonte de verdade atual, derivada 100% do código.

## 1. Visão geral

**SprintJava** é o backend (Java/Spring Boot) do projeto acadêmico
"TOTVS Meeting Intelligence" (disciplina Domain Driven Design – Java,
Challenge 2026). Ele é a peça do meio entre:

- um **frontend Next.js** (fora deste repositório), que envia requisições
  JSON (snake_case) e consome os endpoints REST;
- um **motor de análise em Python** (IA/Ollama, fora deste repositório), que
  recebe a transcrição de uma reunião como arquivo e devolve um relatório de
  análise (triagem, agentes selecionados, relatório final);
- um **banco Oracle** (instância externa da FIAP), acessado via JDBC puro
  (sem JPA/Hibernate).

```
┌──────────┐      HTTP/JSON       ┌──────────┐      HTTP (octet-stream)     ┌──────────┐
│ FRONTEND │ ───────────────────► │   JAVA   │ ───────────────────────────► │  PYTHON  │
│ (Next.js)│ ◄─────────────────── │ (Spring  │ ◄─────────────────────────── │ (uvicorn │
│          │                      │  Boot)   │                              │ +Ollama) │
└──────────┘                      └────┬─────┘                              └──────────┘
                                        │
                                  ┌─────▼──────┐
                                  │  ORACLE    │  (banco externo FIAP)
                                  └────────────┘
```

O caso de uso central: um usuário (vendedor/CSM) cadastra **clientes**,
faz upload da transcrição de uma **reunião**, o motor Python analisa o
conteúdo e devolve achados; esses achados viram **findings** (problemas
recorrentes) que o usuário tenta resolver aplicando **estratégias**
(**attempts**), e o sistema sugere fechamentos (**suggestions**) e monta um
**dashboard** com métricas de sucesso por estratégia.

### Stack

- **Java 21** + **Spring Boot 3.3.4** (`spring-boot-starter-web`,
  `spring-boot-starter-security`, `spring-boot-starter-validation`)
- **Oracle JDBC** (`ojdbc11`) — persistência via DAO/JDBC puro, sem ORM
- **JWT** (`io.jsonwebtoken` / `jjwt`) — autenticação stateless
- **Jackson** com `SNAKE_CASE` global — todo JSON de entrada/saída em
  snake_case, mesmo com campos Java em camelCase
- **JUnit 5** — testes unitários dos `model`
- Empacotado como `sprintjava.jar`, porta padrão **8000**

### Estrutura de pacotes

```
src/main/java/com/sprintjava/
├── SprintjavaApplication.java   # main()
├── model/          # entidades de domínio (regras de negócio + estado)
├── connection/     # ConnectionFactory — abre Connection JDBC com Oracle
├── dao/            # CRUD via JDBC (PreparedStatement) para cada entidade
├── service/        # regras de negócio de agregação + integração com Python
├── controller/      # endpoints REST + controller/dto (DTOs de request/response)
├── security/       # JWT (filtro, serviço de token, entry point de erro)
├── config/         # SecurityConfig
└── exception/      # exceções customizadas + handler global

src/main/resources/
├── application.properties
└── schema.sql       # DDL das tabelas Oracle (não aplicado automaticamente)

src/test/java/com/sprintjava/   # testes JUnit 5 (somente models)
```

---

## 2. Modelo de domínio

Todas as entidades ficam em `model/` como POJOs simples (sem anotações
JPA — são montadas manualmente pelos DAOs a partir do `ResultSet`). A
maior parte da lógica de negócio (validações, máquinas de estado) vive
nos próprios métodos do model, não nos controllers/services.

### User (`model/User.java`)

Representa o usuário do sistema (vendedor/CSM que faz login).

- Campos: `id`, `email`, `passwordHash`, `fullName`, `company`, `jobTitle`,
  `department`, `phone`, `city`, `state`, `linkedinUrl`, `bio`, `createdAt`.
- `emailValido()`: valida o e-mail contra uma regex; usado no cadastro.
- `perfilCompleto()`: `true` quando `company`, `jobTitle` e `department`
  estão todos preenchidos — usado para segmentação comercial.
- **Nunca deve ser exposto com `passwordHash`** para o cliente HTTP — para
  isso existe o DTO `UserResponse`. `UserController`, no entanto, retorna a
  entidade `User` bruta (ver seção 5, observação de segurança).

Relacionamento: 1 User → N Client, N AnalysisJob.

### Client (`model/Client.java`)

Representa uma empresa/cliente gerenciado pelo usuário.

- Campos: `id`, `userId` (dono), `name`, `nameKey`, `segment`,
  `companySize`, `website`, `city`, `state`, `contactName`, `contactRole`,
  `contactEmail`, `contactPhone`, `owner`, `status`, `notes`, `createdAt`.
- Estados (`status`): `prospect` (inicial), `ativo`, `inativo`.
- `gerarNameKey()`: normaliza o nome (minúsculas, sem acento, espaços
  colapsados) para servir de chave de deduplicação — é o que garante a
  constraint `UNIQUE(user_id, name_key)` no banco (dois cadastros do
  "mesmo" cliente com grafias diferentes não colidem por acaso, mas também
  não duplicam por variação trivial de escrita).
- `atualizarStatus(novoStatus)`: valida contra o vocabulário permitido;
  lança `IllegalArgumentException` se inválido (→ 400 pelo handler global).
- `possuiContatoCompleto()`: `true` se nome, e-mail e telefone de contato
  estiverem preenchidos.

Relacionamento: N Client → 1 User; 1 Client → N Meeting, N AnalysisJob,
N Finding.

### Meeting (`model/Meeting.java`)

Representa uma reunião cuja transcrição foi analisada pelo motor Python.

- Campos: `id`, `clientId`, `sourceFilename`, `createdAt`, `triage` (texto
  livre), `selectedAgentsJson` (JSON array serializado como String),
  `finalReportJson` (JSON object serializado), `reportsJson` (JSON array),
  `reviewJson` (JSON object — revisão humana da análise).
- `possuiAnaliseCompleta()`: `true` se `finalReportJson` não for
  nulo/vazio/`"{}"` — indica que o relatório final do Python já foi
  anexado.
- `quantidadeAgentesSelecionados()`: faz parsing manual (sem lib JSON) do
  array `selectedAgentsJson` (remove colchetes, conta elementos separados
  por vírgula); retorna `0` se vazio/curto demais.

Relacionamento: N Meeting → 1 Client; 1 Meeting → N Finding (via
`origin_meeting_id`), N Suggestion (via `triggered_by_meeting_id`); 0..1
AnalysisJob referencia um Meeting como resultado (`meeting_id`).

### AnalysisJob (`model/AnalysisJob.java`)

Representa o job de análise assíncrona disparado no upload de uma
transcrição. É a entidade com a máquina de estados mais explícita do
sistema (e a mais coberta por testes — `AnalysisJobTest`).

- Campos: `id`, `userId`, `clientId`, `sourceFilename`, `inputText`,
  `status`, `errorDetail`, `meetingId` (setado ao concluir com sucesso),
  `createdAt`, `updatedAt`.
- Estados: `queued` (inicial) → `running` → `done` | `failed`.
  `STATUS_RECUPERAVEIS = {queued, running}`.
- `iniciarProcessamento()`: só permitido a partir de `queued`/`running`;
  senão lança `IllegalStateException`. Seta `running`.
- `concluirComSucesso(meetingId)`: exige status atual `running` (senão
  `IllegalStateException`); seta `meetingId`, status `done`, limpa
  `errorDetail`.
- `falhar(motivo)`: seta `failed` + `errorDetail` **incondicionalmente**
  (não valida estado atual — pode falhar a partir de qualquer estado).
- `podeSerRecuperado()`: `true` se o status atual está em
  `{queued, running}` (útil para uma futura rotina de retry/limpeza de
  jobs travados — não implementada ainda).
- `duracaoProcessamentoSegundos()`: `Duration` entre `createdAt` e
  `updatedAt`, `0` se algum for nulo.

Relacionamento: N AnalysisJob → 1 User, N AnalysisJob → 1 Client, 0..1
AnalysisJob → 1 Meeting (resultado).

### Finding (`model/Finding.java`)

Representa um "achado" (problema recorrente detectado numa reunião) que
precisa ser trabalhado até resolução.

- Campos: `id`, `clientId`, `originMeetingId` (opcional), `findingType`,
  `title`, `severityHint`, `status`, `openedAt`, `closedAt`, `attempts`
  (lista carregada à parte via `FindingAttemptDAO`, não persistida
  diretamente na tabela `findings`).
- Estados: `awaiting_strategy` (inicial) → `in_progress` → `resolved` |
  `dismissed` | `recurred`.
- `atualizarStatus(novoStatus)`: valida contra o vocabulário
  (`IllegalArgumentException` se inválido). Se o novo status for
  `resolved` ou `dismissed`, seta `closedAt = now()`; para qualquer outro
  estado, zera `closedAt` (reabertura implícita).

Relacionamento: N Finding → 1 Client; 0..1 Finding → 1 Meeting (origem);
1 Finding → N FindingAttempt; 1 Finding → N Suggestion.

### FindingAttempt (`model/FindingAttempt.java`)

Representa uma tentativa concreta de resolver um finding aplicando uma
estratégia.

- Campos: `id`, `findingId`, `strategyKey`, `strategyLabel`, `notes`,
  `outcome`, `startedAt`, `outcomeAt`.
- Estados (`outcome`): `in_progress` (inicial) → `worked` | `partial` |
  `failed`.
- `registrarResultado(novoOutcome, notas)`: valida o outcome
  (`IllegalArgumentException` se inválido); atualiza `notes` se fornecido;
  se o outcome não for `in_progress`, fecha a tentativa setando
  `outcomeAt = now()`.

Relacionamento: N FindingAttempt → 1 Finding.

### Strategy (`model/Strategy.java`)

Catálogo global de estratégias de resolução disponíveis (não pertence a
um cliente/usuário específico).

- Campos: `id`, `key` (chave de negócio única), `label`, `appliesTo`
  (lista de `finding_type` aos quais se aplica, ou `"*"` para todos),
  `isBuiltin`, `custom`.
- `aplicaA(findingType)`: `true` se `appliesTo` contém `"*"` ou o próprio
  `findingType`.
- Seed inicial (`schema.sql`): `follow_up_call`, `escalate_technical`,
  `pricing_review`, `training_session`.

Sem FK — catálogo compartilhado por todos os usuários.

### Suggestion (`model/Suggestion.java`)

Sugestão de fechamento de um finding, tipicamente gerada após uma nova
análise indicar que o problema foi ou não resolvido.

- Campos: `id`, `findingId`, `triggeredByMeetingId` (opcional), `reason`,
  `status`, `createdAt`.
- Estados persistidos: `open` (inicial) → `accepted` | `rejected` |
  `recurred`.
- Decisões de entrada (vindas do frontend, distintas do status
  persistido): `confirm_resolved`, `still_open`, `recurred`.
- `aplicarDecisao(decisao)`: valida a decisão e traduz para o novo status:
  `confirm_resolved → accepted`, `still_open → rejected`,
  `recurred → recurred`. **Não persiste sozinho** — quem chama grava via
  `SuggestionDAO.atualizar`.

Relacionamento: N Suggestion → 1 Finding; 0..1 Suggestion → 1 Meeting
(gatilho).

---

## 3. Modelo de dados (Oracle)

DDL completo em `src/main/resources/schema.sql`. **Não é aplicado
automaticamente pela aplicação** — as tabelas precisam existir
previamente no Oracle. Todas as PKs são
`NUMBER GENERATED ALWAYS AS IDENTITY`.

| Tabela | Colunas principais | Constraints/Índices |
|---|---|---|
| `users` | email (único), password_hash, full_name, dados de perfil, created_at | — |
| `clients` | user_id (FK→users, CASCADE), name, name_key, status (default `prospect`), dados de contato | `UNIQUE(user_id, name_key)`, índice em `user_id` |
| `meetings` | client_id (FK→clients, CASCADE), source_filename, triage, selected_agents_json, final_report_json, reports_json, review_json | índice em `client_id` |
| `analysis_jobs` | user_id (FK, CASCADE), client_id (FK, CASCADE), source_filename, input_text, status (default `queued`), error_detail, meeting_id (FK→meetings, SET NULL) | índices em `user_id`, `client_id` |
| `findings` | client_id (FK, CASCADE), origin_meeting_id (FK→meetings, SET NULL), finding_type, title, severity_hint, status (default `awaiting_strategy`), opened_at, closed_at | índice em `client_id` |
| `finding_attempts` | finding_id (FK→findings, CASCADE), strategy_key, strategy_label, notes, outcome (default `in_progress`), started_at, outcome_at | índice em `finding_id` |
| `strategies` | strategy_key (único), label, applies_to_json, is_builtin, custom | sem FK (catálogo global) |
| `suggestions` | finding_id (FK→findings, CASCADE), triggered_by_meeting_id (FK→meetings, SET NULL), reason, status (default `open`), created_at | índice em `finding_id` |

Diagrama conceitual de relacionamentos:

```
users (1) ──< clients (1) ──< meetings
                │                  │
                ├──< analysis_jobs ┤ (meeting_id opcional = resultado)
                │
                └──< findings ──< finding_attempts
                          │
                          └──< suggestions >── meetings (opcional)

strategies  (catálogo isolado, sem FK)
```

---

## 4. Camada de acesso a dados (DAO)

Todos os DAOs em `dao/` são `@Repository`, usam **JDBC puro** com
`PreparedStatement`. Cada método abre e fecha sua própria `Connection`
via try-with-resources (`ConnectionFactory.getConnection()`) — **não há
pool de conexões nem transação compartilhada entre chamadas**. IDs
gerados são lidos via `getGeneratedKeys()`.

| DAO | Operações | Observações |
|---|---|---|
| `UserDAO` | inserir, buscarPorId, buscarPorEmail, listarTodos, atualizar, deletar | sem joins |
| `ClientDAO` | inserir, buscarPorId, listarPorUsuario, listarTodos, atualizar, deletar | sem joins |
| `MeetingDAO` | inserir, buscarPorId, listarPorCliente, listarTodas, atualizar, deletar | sem joins |
| `AnalysisJobDAO` | inserir, buscarPorId, listarPorStatus (fila FIFO por `created_at`), listarTodos, atualizar, deletar | trata `meeting_id` nullable |
| `FindingDAO` | inserir, buscarPorId, listarPorCliente, **listarPorUsuario** (JOIN com `clients`), atualizar, deletar | `listarPorUsuario` alimenta o dashboard geral |
| `FindingAttemptDAO` | inserir, buscarPorId, listarPorFinding, **listarPorUsuario** (double JOIN `finding_attempts→findings→clients`), atualizar, deletar | `listarPorUsuario` existe mas não há chamador atual nos controllers |
| `StrategyDAO` | inserir, listarTodas, buscarPorKey, atualizar, deletar | serializa/desserializa `appliesTo` via Jackson; erro de parse retorna lista vazia silenciosamente |
| `SuggestionDAO` | inserir, buscarPorId, **listarAbertasPorUsuario** (JOIN triplo), **listarAbertasPorCliente** (JOIN duplo), atualizar, deletar | ambos os métodos filtram `status = 'open'` |

---

## 5. Camada de serviço

### DashboardService (`service/DashboardService.java`)

Lógica pura de agregação em memória (sem acesso a banco) — recebe listas
já carregadas pelos controllers e monta o payload do dashboard
(`montar(scope, clientId, findings, openSuggestions, findingsPorId)`):

1. **openQueue** — findings em `awaiting_strategy`/`in_progress`,
   ordenados por `openedAt` (nulls por último).
2. **severeWithoutActiveAttempt** — findings com `severityHint`
   preenchido e nenhuma tentativa `in_progress`.
3. **strategyRanking** — agrupa todas as `FindingAttempt` por
   `strategyKey`; calcula `worked`/`failed`/`partial`/`inProgress`,
   `total`, `taxaSucesso = worked / (worked+failed+partial)` (`null` se
   nenhuma tentativa concluída), e `lowSample = total < 5` (sinaliza
   estratégias com amostra estatisticamente pouco confiável).
4. **avgResolutionSeconds** — média de `closedAt - openedAt` só para
   findings `resolved` com ambos os timestamps preenchidos; `null` se
   nenhum qualificar.
5. **statusCounts** — contagem de findings por cada um dos 5 status.
6. **totals** — `findings` (total), `open` (soma dos 2 status abertos),
   `attempts` (soma de todas as tentativas).
7. **openSuggestions** — sugestões abertas combinadas com o finding
   correspondente.

### ServicoAnalisePython (`service/ServicoAnalisePython.java`)

Integração HTTP com o motor Python, via `java.net.http.HttpClient`.

- Endpoint: `http://localhost:5000/analisar` por padrão, sobrescrito pela
  variável de ambiente `PYTHON_ANALISE_URL` (no `docker-compose.yml`:
  `http://python:8000/analisar`).
- Timeouts: **5s** para conectar, **25 minutos** para resposta — reflete
  que a análise por IA (Ollama) pode ser demorada.
- `enviarParaProcessamento(bytes, nomeArquivo)`: `POST` com
  `Content-Type: application/octet-stream` + header `X-File-Name`, corpo
  = bytes brutos do arquivo (upload repassado como binário, não
  multipart). Se o status HTTP ≠ 200, lança `AnalisePythonException` com
  status e corpo da resposta. Trata separadamente `HttpTimeoutException`,
  `IOException` ("Python indisponível") e `InterruptedException`.

> **Observação importante sobre a "assincronia":** a máquina de estados
> `queued → running → done/failed` existe no modelo `AnalysisJob`, mas a
> chamada HTTP de upload (`AnalysisController.upload`) é executada de
> forma **síncrona** dentro do próprio request — não há `@Async`,
> `CompletableFuture`, fila ou thread separada. O request de upload fica
> bloqueado até a análise Python terminar (podendo levar até 25 minutos,
> daí `server.tomcat.connection-timeout=1800000` em
> `application.properties`). O endpoint de polling
> (`GET /api/analysis/jobs/{id}`) existe e funciona, mas na implementação
> atual o job já estará em seu estado final quando o upload retornar.

### AnalisePythonException (`service/AnalisePythonException.java`)

Checked exception simples usada por `ServicoAnalisePython` para sinalizar
falhas de comunicação/timeout/status de erro do serviço Python.

---

## 6. Segurança

Configuração em `config/SecurityConfig.java` + `security/`.

- **Modelo**: JWT stateless (`SessionCreationPolicy.STATELESS`), CSRF
  desabilitado, sem RBAC (não há papéis/authorities — qualquer usuário
  autenticado acessa qualquer endpoint; a posse de dados é checada
  manualmente em cada controller comparando `userId`).
- **Rotas públicas**: apenas `POST /api/auth/login` e
  `POST /api/auth/register`. Todo o resto exige `Authorization: Bearer
  <token>`.
- **Filtro** (`JwtAuthenticationFilter`): lê o header, extrai `userId` via
  `JwtService.extrairUserId`, busca o `User` no `UserDAO` e popula o
  `SecurityContextHolder`. Se o token estiver ausente/inválido, a
  requisição segue sem autenticação — quem responde 401 é o
  `SecurityConfig` (`anyRequest().authenticated()`) via
  `JsonAuthenticationEntryPoint`, que retorna
  `{"detail": "Não autenticado"}`.
- **JwtService**: `gerarToken(userId, email)` usa `Jwts.builder()`
  (subject = userId, claim `email`, HMAC via chave secreta);
  `extrairUserId(token)` valida assinatura e expiração.
- **Configuração** (`application.properties`):
  `app.jwt.secret` (env `JWT_SECRET`, com default **inseguro** de
  desenvolvimento) e `app.jwt.expiration-minutes=480` (8h).
- **PasswordEncoder**: `BCryptPasswordEncoder`, usado em `AuthController`
  para hash e verificação de senha.

---

## 7. Tratamento de erros

`exception/GlobalExceptionHandler.java` (`@RestControllerAdvice`)
padroniza todas as respostas de erro no formato `{"detail": ...}`:

| Exceção | Status | Corpo |
|---|---|---|
| `ApiException` | o da própria exceção | `{"detail": "mensagem"}` |
| `BadCredentialsException` | 401 | `{"detail": "Credenciais inválidas"}` |
| `ResponseStatusException` | o da própria exceção | reason/message |
| `MethodArgumentNotValidException` (`@Valid`) | 422 | `{"detail": [{"msg": "campo: mensagem"}, ...]}` (formato lista, estilo Pydantic/FastAPI) |
| `IllegalArgumentException` (validações de model) | 400 | mensagem da exceção |
| `SQLException` | 500 | mensagem genérica (não vaza detalhes internos) |
| `Exception` (catch-all) | 500 | mensagem genérica |

`ApiException` carrega `HttpStatus` + mensagem, usada explicitamente nos
controllers para erros de negócio (404, 409, 422 etc.).

---

## 8. Conexão com o banco

`connection/ConnectionFactory.java` conecta via
`DriverManager.getConnection(...)` a
`jdbc:oracle:thin:@oracle.fiap.com.br:1521:orcl`, com usuário e senha
**hardcoded no código-fonte** — requisito explícito da disciplina (não é
prática recomendada para produção). Cada chamada cria uma nova conexão
(sem pool); os DAOs preferem try-with-resources para fechá-la.

---

## 9. Configuração da aplicação

`src/main/resources/application.properties`:

```properties
server.port=8000
spring.application.name=sprintjava
spring.servlet.multipart.max-file-size=160MB
spring.servlet.multipart.max-request-size=160MB
server.tomcat.connection-timeout=1800000        # 30 min — alinhado ao upload/análise demorada
spring.jackson.property-naming-strategy=SNAKE_CASE
app.jwt.secret=${JWT_SECRET:mude-esta-chave-em-producao-para-um-valor-secreto-com-32-bytes}
app.jwt.expiration-minutes=480
```

`SprintjavaApplication.java` é a classe mínima `@SpringBootApplication`
com o `main()`.

---

## 10. Endpoints REST

Todos os controllers obtêm o usuário autenticado via
`((User) authentication.getPrincipal()).getId()` para checar posse dos
recursos (multi-tenant por `user_id`).

### Autenticação — `AuthController` (`/api/auth`)

| Método | Path | Corpo | Observações |
|---|---|---|---|
| POST | `/api/auth/login` | form-urlencoded `username`+`password` | público; retorna `LoginResponse` (token + `UserResponse`) |
| POST | `/api/auth/register` | JSON `RegisterRequest` | público; 409 se e-mail duplicado; 422 se e-mail inválido |
| GET | `/api/auth/me` | — | retorna `UserResponse` do usuário logado |
| PATCH | `/api/auth/me` | `UpdateProfileRequest` | patch parcial (campos nulos não alteram) |

### Usuários — `UserController` (`/api/users`)

| Método | Path | Observações |
|---|---|---|
| GET | `/api/users` | lista todos os usuários — retorna entidade `User` **bruta** (não `UserResponse`) |
| GET | `/api/users/{id}` | idem, 404 se não existir |

> ⚠️ Ambos endpoints retornam `User` diretamente, incluindo
> `passwordHash` no JSON serializado — potencial exposição de dado
> sensível (diferente do padrão seguido em `AuthController`, que usa
> `UserResponse` para nunca vazar o hash).

### Clientes — `ClientController` (`/api/clients`)

| Método | Path | Observações |
|---|---|---|
| GET | `/api/clients` | lista clientes do usuário, com `meetingsCount`/`lastMeetingAt` |
| GET | `/api/clients/{id}` | `ClientDetailResponse` com lista de meetings; 404 se não pertence ao usuário |
| POST | `/api/clients` | cria cliente (`status` inicial `prospect`); 422 se `name` ausente |
| PATCH | `/api/clients/{id}` | patch parcial; recalcula `nameKey` se `name` mudar; `atualizarStatus()` pode 400 |

### Reuniões — `MeetingController` (`/api/meetings`)

| Método | Path | Observações |
|---|---|---|
| GET | `/api/meetings/{id}` | `MeetingDetailResponse` (parse de JSONs armazenados) |
| DELETE | `/api/meetings/{id}` | 204 No Content |
| GET | `/api/meetings/{id}/review` | 404 se ainda não há revisão |
| POST | `/api/meetings/{id}/review` | merge de itens de revisão por `axis`; decisão `confirm/dismiss/pending` |

### Dashboard — `DashboardController`

| Método | Path | Observações |
|---|---|---|
| GET | `/api/dashboard` | dashboard geral do usuário (todos os clientes) |
| GET | `/api/clients/{id}/dashboard` | dashboard restrito a um cliente |

### Estratégias — `StrategyController` (`/api/strategies`)

| Método | Path | Observações |
|---|---|---|
| GET | `/api/strategies?finding_type=X` | lista o catálogo, filtra opcionalmente por tipo de finding |

### Sugestões — `SuggestionController` (`/api/suggestions`)

| Método | Path | Observações |
|---|---|---|
| POST | `/api/suggestions/{id}` | aplica uma `decision`; atualiza suggestion + finding relacionados |

### Findings — `FindingController` (`/api/findings`)

| Método | Path | Observações |
|---|---|---|
| GET | `/api/findings?client_id=X` | lista findings do cliente com `attempts` |
| PATCH | `/api/findings/{id}` | patch parcial; `atualizarStatus()` pode 400 |
| POST | `/api/findings/{id}/attempts` | cria tentativa, força finding para `in_progress` |
| PATCH | `/api/findings/{id}/attempts/{attemptId}` | `registrarResultado()`; 404 se a tentativa não pertence ao finding |

### Análise — `AnalysisController` (`/api/analysis`)

| Método | Path | Observações |
|---|---|---|
| POST | `/api/analysis/upload` | multipart `file` + `client_id`; **executa a análise Python de forma síncrona** dentro do request; cria `Meeting` e atualiza o `AnalysisJob` |
| GET | `/api/analysis/jobs/{id}` | status do job + `MeetingDetailResponse` se já concluído |

Fluxo de análise: upload cria um `AnalysisJob` (`queued`) →
`iniciarProcessamento()` (`running`) → envia o arquivo ao Python →
parseia `triage`/`selected_agents`/`final_report`/`reports` da resposta →
cria `Meeting` → `concluirComSucesso(meetingId)` (`done`). Em caso de
`AnalisePythonException`/`IOException`/`SQLException`, chama `falhar()`
(`failed`).

---

## 11. Testes

Testes unitários **JUnit 5**, restritos à camada `model` (não há testes
de controller/DAO/security/integração):

- `AppTest` — smoke test trivial.
- `UserTest` — `emailValido()` e `perfilCompleto()`.
- `ClientTest` — `gerarNameKey()` (normalização de acentos/espaços),
  status inicial `prospect`, `atualizarStatus()` válido/inválido,
  `possuiContatoCompleto()`.
- `MeetingTest` — `possuiAnaliseCompleta()` e
  `quantidadeAgentesSelecionados()`.
- `AnalysisJobTest` — cobre toda a máquina de estados: fluxo feliz
  (`queued→running→done`), falha a partir de estado inválido
  (`IllegalStateException`), `falhar()` a partir de qualquer estado,
  `podeSerRecuperado()`.

Não cobertos por teste automatizado: DAOs (exigem Oracle real),
controllers (sem `MockMvc`), segurança (JWT), `DashboardService`,
`ServicoAnalisePython`.

---

## 12. Como rodar

### Local (Maven)

```bash
mvn spring-boot:run
```

API sobe em `http://localhost:8000`.

### Docker

```bash
docker network create sprintjava-net     # só na 1ª vez (rede externa compartilhada)
docker compose up -d --build
docker compose logs -f backend
docker compose down
```

Pré-requisitos: acesso de rede ao Oracle da FIAP
(`oracle.fiap.com.br:1521:orcl`), rede `sprintjava-net` criada previamente
(compartilhada com o container Python), e `PYTHON_ANALISE_URL` apontando
para o serviço Python.

### Testes

```bash
mvn test
```

---

## 13. Pontos de atenção conhecidos (achados na análise do código)

- `UserController` (`GET /api/users`, `GET /api/users/{id}`) retorna a
  entidade `User` bruta, incluindo `passwordHash` — deveria usar
  `UserResponse` como o restante da API.
- `ConnectionFactory` tem usuário/senha do Oracle hardcoded — assumido
  como requisito da disciplina, não recomendado para produção real.
- O fluxo de upload/análise (`AnalysisController.upload`) é síncrono de
  ponta a ponta apesar do modelo de dados sugerir um job assíncrono — o
  request HTTP fica bloqueado durante toda a chamada ao Python (até 25
  min).
- `FindingAttemptDAO.listarPorUsuario` existe mas não é chamado por
  nenhum controller atualmente (código morto ou funcionalidade
  incompleta).
- `app.jwt.secret` tem valor default inseguro — deve ser sobrescrito via
  `JWT_SECRET` em qualquer ambiente real.
- Não há autorização por papel (RBAC); a única barreira de acesso é
  "estar autenticado" + checagem manual de `userId` dono do recurso em
  cada controller.
