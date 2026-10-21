-- Sem DEFAULT: um valor temporário (ex. 0.01) passaria todas as validações de negócio
-- e seria copiado silenciosamente para o lançamento quando essa feature existir, sem
-- que ninguém percebesse o dado errado. Não há ambiente de produção nem db/testdata
-- hoje (Testcontainers recria o schema do zero em cada suíte), então é preferível que
-- esta migration falhe de forma visível num banco local que já tenha apartamentos
-- cadastrados do que gravar um valor falso para eles.
ALTER TABLE apartamento ADD COLUMN valor_aluguel NUMERIC(10,2) NOT NULL;

ALTER TABLE apartamento ADD CONSTRAINT ck_apartamento_valor_aluguel_positivo CHECK (valor_aluguel > 0);
