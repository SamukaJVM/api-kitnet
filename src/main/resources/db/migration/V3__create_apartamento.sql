CREATE TABLE apartamento (
    id BIGSERIAL PRIMARY KEY,
    numero VARCHAR(10) NOT NULL,
    descricao VARCHAR(90) NOT NULL,
    medidor VARCHAR(10) UNIQUE,
    status VARCHAR(20) NOT NULL DEFAULT 'DISPONIVEL',
    predio_id BIGINT NOT NULL,
    created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    updated_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    CONSTRAINT fk_apartamento_predio FOREIGN KEY (predio_id) REFERENCES predio (id),
    -- A UNIQUE abaixo já cria um índice iniciado por predio_id, que cobre tanto a
    -- checagem de FK quanto o filtro por prédio na listagem; um índice extra em
    -- predio_id seria redundante e só encareceria a escrita.
    CONSTRAINT uk_apartamento_predio_numero UNIQUE (predio_id, numero)
);
