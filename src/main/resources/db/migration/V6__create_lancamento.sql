CREATE TABLE lancamento (
    id BIGSERIAL PRIMARY KEY,
    apartamento_id BIGINT NOT NULL REFERENCES apartamento (id),
    inquilino_id BIGINT NOT NULL REFERENCES inquilino (id),
    data_entrada DATE NOT NULL,
    data_pagamento DATE NOT NULL,
    valor_mensal NUMERIC(10,2) NOT NULL,
    dias INTEGER NOT NULL,
    valor_total NUMERIC(10,2) NOT NULL,
    valor_pago NUMERIC(10,2) NOT NULL DEFAULT 0,
    valor_debito NUMERIC(10,2) NOT NULL,
    status VARCHAR(20) NOT NULL DEFAULT 'ABERTO',
    status_pagamento VARCHAR(20) NOT NULL,
    observacao VARCHAR(255),
    created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    updated_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP
);

CREATE INDEX ix_lancamento_apartamento ON lancamento (apartamento_id);
CREATE INDEX ix_lancamento_inquilino ON lancamento (inquilino_id);

-- Só pode existir um lançamento ABERTO por apartamento/por inquilino. Garante no banco,
-- de forma atômica, a regra que a checagem em Java (apartamento.status == DISPONIVEL) não
-- consegue garantir sozinha sob concorrência (duas requisições lendo DISPONIVEL ao mesmo tempo).
CREATE UNIQUE INDEX ux_lancamento_apartamento_aberto ON lancamento (apartamento_id) WHERE status = 'ABERTO';
CREATE UNIQUE INDEX ux_lancamento_inquilino_aberto ON lancamento (inquilino_id) WHERE status = 'ABERTO';
