CREATE TABLE predio (
    id BIGSERIAL PRIMARY KEY,
    descricao VARCHAR(90) NOT NULL UNIQUE,
    cep CHAR(8) NOT NULL,
    logradouro VARCHAR(255),
    complemento VARCHAR(255),
    bairro VARCHAR(90),
    localidade VARCHAR(90),
    uf CHAR(2),
    numero VARCHAR(10),
    created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    updated_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP
);
