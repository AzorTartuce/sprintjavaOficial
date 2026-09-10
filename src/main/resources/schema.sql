-- Schema Oracle para o TOTVS Meeting Intelligence (Sprint 3/4)
-- Espelha 1:1 as entidades do model Java e o contrato de tipos do frontend
-- (ver docs/frontinformacoes.md, seção 7).

CREATE TABLE users (
    id             NUMBER GENERATED ALWAYS AS IDENTITY PRIMARY KEY,
    email          VARCHAR2(200) NOT NULL UNIQUE,
    password_hash  VARCHAR2(200) NOT NULL,
    full_name      VARCHAR2(200) NOT NULL,
    company        VARCHAR2(200),
    job_title      VARCHAR2(120),
    department     VARCHAR2(120),
    phone          VARCHAR2(40),
    city           VARCHAR2(120),
    state          VARCHAR2(2),
    linkedin_url   VARCHAR2(300),
    bio            CLOB,
    created_at     TIMESTAMP NOT NULL
);

CREATE TABLE clients (
    id             NUMBER GENERATED ALWAYS AS IDENTITY PRIMARY KEY,
    user_id        NUMBER NOT NULL REFERENCES users(id) ON DELETE CASCADE,
    name           VARCHAR2(200) NOT NULL,
    name_key       VARCHAR2(200) NOT NULL,
    segment        VARCHAR2(120),
    company_size   VARCHAR2(40),
    website        VARCHAR2(300),
    city           VARCHAR2(120),
    state          VARCHAR2(2),
    contact_name   VARCHAR2(200),
    contact_role   VARCHAR2(120),
    contact_email  VARCHAR2(200),
    contact_phone  VARCHAR2(40),
    owner          VARCHAR2(200),
    status         VARCHAR2(40) DEFAULT 'prospect' NOT NULL,
    notes          CLOB,
    created_at     TIMESTAMP NOT NULL,
    CONSTRAINT uq_clients_user_namekey UNIQUE (user_id, name_key)
);

CREATE INDEX idx_clients_user_id ON clients(user_id);

CREATE TABLE meetings (
    id                   NUMBER GENERATED ALWAYS AS IDENTITY PRIMARY KEY,
    client_id            NUMBER NOT NULL REFERENCES clients(id) ON DELETE CASCADE,
    source_filename      VARCHAR2(500) DEFAULT '' NOT NULL,
    created_at           TIMESTAMP NOT NULL,
    triage               CLOB DEFAULT '' NOT NULL,
    selected_agents_json CLOB DEFAULT '[]' NOT NULL,
    final_report_json    CLOB DEFAULT '{}' NOT NULL,
    reports_json         CLOB DEFAULT '[]' NOT NULL,
    review_json          CLOB DEFAULT '{}' NOT NULL
);

CREATE INDEX idx_meetings_client_id ON meetings(client_id);

CREATE TABLE analysis_jobs (
    id               NUMBER GENERATED ALWAYS AS IDENTITY PRIMARY KEY,
    user_id          NUMBER NOT NULL REFERENCES users(id) ON DELETE CASCADE,
    client_id        NUMBER NOT NULL REFERENCES clients(id) ON DELETE CASCADE,
    source_filename  VARCHAR2(500) NOT NULL,
    input_text       CLOB,
    status           VARCHAR2(20) DEFAULT 'queued' NOT NULL,
    error_detail     CLOB,
    meeting_id       NUMBER REFERENCES meetings(id) ON DELETE SET NULL,
    created_at       TIMESTAMP NOT NULL,
    updated_at       TIMESTAMP NOT NULL
);

CREATE INDEX idx_analysis_jobs_user_id ON analysis_jobs(user_id);
CREATE INDEX idx_analysis_jobs_client_id ON analysis_jobs(client_id);

-- Finding = achado recorrente detectado numa reunião (ver Finding em types.ts).
CREATE TABLE findings (
    id                 NUMBER GENERATED ALWAYS AS IDENTITY PRIMARY KEY,
    client_id          NUMBER NOT NULL REFERENCES clients(id) ON DELETE CASCADE,
    origin_meeting_id  NUMBER REFERENCES meetings(id) ON DELETE SET NULL,
    finding_type       VARCHAR2(120) NOT NULL,
    title              VARCHAR2(300) NOT NULL,
    severity_hint      VARCHAR2(40),
    status             VARCHAR2(40) DEFAULT 'awaiting_strategy' NOT NULL,
    opened_at          TIMESTAMP,
    closed_at          TIMESTAMP
);

CREATE INDEX idx_findings_client_id ON findings(client_id);

-- Attempt = tentativa de resolução aplicada a um finding (ver Attempt em types.ts).
CREATE TABLE finding_attempts (
    id               NUMBER GENERATED ALWAYS AS IDENTITY PRIMARY KEY,
    finding_id       NUMBER NOT NULL REFERENCES findings(id) ON DELETE CASCADE,
    strategy_key     VARCHAR2(120) NOT NULL,
    strategy_label   VARCHAR2(300),
    notes            CLOB,
    outcome          VARCHAR2(40) DEFAULT 'in_progress' NOT NULL,
    started_at       TIMESTAMP,
    outcome_at       TIMESTAMP
);

CREATE INDEX idx_finding_attempts_finding_id ON finding_attempts(finding_id);

-- Strategy = catálogo de estratégias de resolução (ver StrategyOption em types.ts).
CREATE TABLE strategies (
    id             NUMBER GENERATED ALWAYS AS IDENTITY PRIMARY KEY,
    strategy_key   VARCHAR2(120) NOT NULL UNIQUE,
    label          VARCHAR2(300) NOT NULL,
    applies_to_json CLOB DEFAULT '[]' NOT NULL,
    is_builtin     NUMBER(1) DEFAULT 1 NOT NULL,
    custom         NUMBER(1) DEFAULT 0 NOT NULL
);

-- Suggestion = sugestão de fechamento de um finding (ver OpenSuggestion em types.ts).
CREATE TABLE suggestions (
    id                      NUMBER GENERATED ALWAYS AS IDENTITY PRIMARY KEY,
    finding_id              NUMBER NOT NULL REFERENCES findings(id) ON DELETE CASCADE,
    triggered_by_meeting_id NUMBER REFERENCES meetings(id) ON DELETE SET NULL,
    reason                  CLOB,
    status                  VARCHAR2(40) DEFAULT 'open' NOT NULL,
    created_at              TIMESTAMP
);

CREATE INDEX idx_suggestions_finding_id ON suggestions(finding_id);

-- Catálogo inicial de estratégias (builtin) para o front ter algo a listar.
INSERT INTO strategies (strategy_key, label, applies_to_json, is_builtin, custom) VALUES
    ('follow_up_call', 'Ligação de acompanhamento', '["*"]', 1, 0);
INSERT INTO strategies (strategy_key, label, applies_to_json, is_builtin, custom) VALUES
    ('escalate_technical', 'Escalar para time técnico', '["technical_issue","integration_gap"]', 1, 0);
INSERT INTO strategies (strategy_key, label, applies_to_json, is_builtin, custom) VALUES
    ('pricing_review', 'Revisão comercial/pricing', '["pricing_concern","churn_risk"]', 1, 0);
INSERT INTO strategies (strategy_key, label, applies_to_json, is_builtin, custom) VALUES
    ('training_session', 'Sessão de treinamento', '["adoption_gap","usability_issue"]', 1, 0);

COMMIT;
