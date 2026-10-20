-- Usuarios de prueba: admin/admin12345, editor/editor12345, viewer/viewer12345
INSERT INTO usuarios (id, username, password_hash, enabled) VALUES
    (1, 'admin', '$2a$10$YOv3md4ptvwFkJWC5IFPfulnw8kBc8uPaj4I9xaqk4fWQdMwn.NlW', 1),
    (2, 'editor', '$2a$10$sp0enSxn0V2rfXb1mb.5SuhLlRVrC0Qo9gJugt2keHDX.TFEfyupO', 1),
    (3, 'viewer', '$2a$10$///T7HA5PkrZHOw0pdIhnO7V936g9x6izEXpCutBBpOri5x6.CTPe', 1);
