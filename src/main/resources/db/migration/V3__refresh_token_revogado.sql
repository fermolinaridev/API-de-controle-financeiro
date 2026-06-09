CREATE TABLE refresh_token_revogado (
    jti VARCHAR(64) PRIMARY KEY,
    expires_at TIMESTAMP NOT NULL,
    revogado_em TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP
);

CREATE INDEX idx_refresh_revogado_expires_at ON refresh_token_revogado(expires_at);
