# sprintjava

Backend Java (Spring Boot) do sistema **SprintJava** — projeto acadêmico da disciplina
**Domain Driven Design – Java** (Challenge 2026 / TOTVS). É o serviço do meio entre o
front-end (Next.js) e o motor de análise em Python (IA/Ollama), com persistência em Oracle.

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

## Stack

- **Java 21** + **Spring Boot 3.3.4** (`spring-boot-starter-web`, `spring-boot-starter-security`, `spring-boot-starter-validation`)
- **Oracle JDBC** (`ojdbc11`) — persistência via DAO/JDBC puro (sem JPA)
- **JWT** (`jjwt`) — autenticação stateless
- **JUnit 5** — testes dos métodos de negócio do `model`
- Empacotado como `sprintjava.jar`, porta padrão **8000**

## Estrutura do projeto

```
src/main/java/com/sprintjava/
├── SprintjavaApplication.java   # main()
├── model/                       # entidades de domínio (User, Client, Meeting, AnalysisJob,
│                                 # Finding, FindingAttempt, Strategy, Suggestion)
├── connection/                   # ConnectionFactory — abre Connection JDBC com Oracle
├── dao/                          # CRUD via JDBC (PreparedStatement) para cada entidade
├── service/                      # regras de negócio, integração com o serviço Python
├── controller/                   # endpoints REST + DTOs (controller/dto)
├── security/                     # JWT (filtro, serviço de token, entry point)
├── config/                       # SecurityConfig
└── exception/                    # exceções customizadas + handler global

src/main/resources/
├── application.properties
└── schema.sql                    # DDL das tabelas (Oracle)

src/test/java/com/sprintjava/     # testes dos models (JUnit 5)
```

## Modelo de dados

Tabelas em `src/main/resources/schema.sql`:

`users`, `clients`, `meetings`, `analysis_jobs`, `findings`, `finding_attempts`,
`strategies`, `suggestions`.

> O `schema.sql` **não** é aplicado automaticamente — as tabelas precisam existir
> previamente no Oracle.

## Como rodar

### Localmente (Maven)

```bash
mvn spring-boot:run
```

A API sobe em `http://localhost:8000`.

### Via Docker

```bash
docker network create sprintjava-net     # só na 1ª vez (rede externa compartilhada)
docker compose up -d --build              # builda e sobe o container "backend"
docker compose logs -f backend            # acompanhar logs
docker compose down                       # parar e remover
```

Pré-requisitos:
- Acesso de rede ao Oracle da FIAP (`oracle.fiap.com.br:1521:orcl`) — sem isso a app sobe mas falha ao consultar o banco.
- A rede `sprintjava-net` precisa existir antes do `up` (é compartilhada com o container Python).
- Variável de ambiente `PYTHON_ANALISE_URL` configurada no `docker-compose.yml`, apontando para o serviço Python (ex.: `http://python:8000/analisar`).

### Testes

```bash
mvn test
```

## Autenticação

- JWT stateless via Spring Security (`config/SecurityConfig.java`).
- Header: `Authorization: Bearer {token}`.
- Rotas públicas: `POST /api/auth/login` e `POST /api/auth/register`. Todas as demais exigem token.
- Todo JSON de request/response usa **snake_case** (configurado em `application.properties`).

## Principais endpoints

| Método | Path | Obs |
|---|---|---|
| POST | `/api/auth/login` | `application/x-www-form-urlencoded`, campos `username`+`password` |
| POST | `/api/auth/register` | JSON snake_case |
| GET | `/api/auth/me` | usuário logado |
| GET/POST | `/api/clients`, `/api/clients/{id}` | CRUD de clientes |
| GET | `/api/dashboard`, `/api/clients/{id}/dashboard` | métricas |
| GET | `/api/findings` | filtrável por `client_id` |
| POST | `/api/findings/{id}/attempts` | tentativas de finding |
| GET/DELETE | `/api/meetings/{id}` | reunião analisada |
| GET/POST | `/api/meetings/{id}/review` | revisão de reunião |
| GET | `/api/strategies` | estratégias |
| POST | `/api/suggestions/{id}` | decidir sugestão |
| POST | `/api/analysis/upload` | multipart: `file` + `client_id` — inicia job de análise |
| GET | `/api/analysis/jobs/{id}` | polling do status do job |

Fluxo de análise: upload cria um `AnalysisJob` (`queued → running → done/failed`) que é
processado de forma assíncrona pelo `service/ServicoAnalisePython.java`, que envia o arquivo
para o motor Python e grava o resultado como `Meeting`.

## Documentação adicional

- `docs/BACKEND_JAVA_COMPLETO.md` — arquitetura detalhada, integração com front/Python e histórico de problemas resolvidos
- `docs/FRONTEND_INTEGRATION.md` — contrato de endpoints para o front-end
- `docs/LOGICA_ANALISE_PYTHON.md` — contrato Java ↔ Python
- `docs/start.md` — decisões e roteiro do projeto
- `ESTRUTURA_PROJETO_JAVA.md` — critérios de avaliação e estrutura esperada (Sprint 3/4)
