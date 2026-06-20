CREATE TABLE investimento (
    id BIGSERIAL PRIMARY KEY,
    ticker VARCHAR(20) NOT NULL,
    nome VARCHAR(120),
    classe VARCHAR(10) NOT NULL CHECK (classe IN ('ACAO', 'FII', 'BDR', 'CRIPTO', 'OUTRO')),
    quantidade NUMERIC(18,8) NOT NULL CHECK (quantidade > 0),
    preco_medio NUMERIC(18,8) NOT NULL CHECK (preco_medio > 0),
    usuario_id BIGINT NOT NULL REFERENCES usuario(id),
    criado_em TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP
);

CREATE INDEX idx_investimento_usuario ON investimento(usuario_id);
