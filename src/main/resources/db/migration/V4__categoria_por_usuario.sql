-- usuario_id NULL = categoria do sistema (visível a todos, imutável);
-- preenchido = categoria pessoal do usuário
ALTER TABLE categoria ADD COLUMN usuario_id BIGINT REFERENCES usuario(id);

CREATE INDEX idx_categoria_usuario ON categoria(usuario_id);
