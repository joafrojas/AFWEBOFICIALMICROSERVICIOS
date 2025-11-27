-- Vista: roles_users_view
-- Muestra por fila: id_usuario, nombre_usuario, rol
-- Ejecuta este script en HeidiSQL o mysql CLI conectado a la base `usuarios_db`.

CREATE OR REPLACE VIEW roles_users_view AS
SELECT
  u.id AS id_usuario,
  COALESCE(u.nombre_usuario, u.nombreUsuario, u.nombre_usu, u.nombre) AS nombre_usuario,
  COALESCE(r.name, u.rol) AS rol
FROM usuarios u
LEFT JOIN roles r ON u.role_id = r.id;

-- Uso: SELECT * FROM roles_users_view;
