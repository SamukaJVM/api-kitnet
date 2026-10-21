CREATE TABLE inquilino (
    id BIGSERIAL PRIMARY KEY,
    nome VARCHAR(90) NOT NULL,
    nome_abreviado VARCHAR(20) NOT NULL,
    email VARCHAR(90),
    contato VARCHAR(15),
    cpf VARCHAR(11),
    genero VARCHAR(20),
    status VARCHAR(20) NOT NULL DEFAULT 'ATIVO',
    created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    updated_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    CONSTRAINT uk_inquilino_cpf UNIQUE (cpf)
);
