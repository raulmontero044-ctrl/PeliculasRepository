CREATE TABLE usuarios (
  id BIGINT AUTO_INCREMENT PRIMARY KEY,
  username VARCHAR(50) NOT NULL,
  password_hash VARCHAR(255) NOT NULL,
  enabled TINYINT(1) NOT NULL DEFAULT 1,
  CONSTRAINT uk_usuarios_username UNIQUE (username)
);

CREATE TABLE auth_tokens (
  id BIGINT AUTO_INCREMENT PRIMARY KEY,
  token VARCHAR(128) NOT NULL,
  user_id BIGINT NOT NULL,
  tipo VARCHAR(10) NOT NULL,
  expires_at TIMESTAMP NOT NULL,
  created_at TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP,
  CONSTRAINT uk_auth_tokens_token UNIQUE (token),
  CONSTRAINT fk_auth_tokens_usuario FOREIGN KEY (user_id) REFERENCES usuarios(id) ON DELETE RESTRICT,
  CONSTRAINT chk_auth_tokens_tipo CHECK (tipo IN ('ACCESS','REFRESH'))
);

CREATE INDEX idx_auth_tokens_token ON auth_tokens(token);
CREATE INDEX idx_auth_tokens_user_id ON auth_tokens(user_id);
CREATE INDEX idx_auth_tokens_expires_at ON auth_tokens(expires_at);
