-- View: roles_with_users
-- Shows role info together with user id and username
CREATE OR REPLACE VIEW roles_with_users AS
SELECT r.id AS role_id,
       r.name AS role_name,
       u.id AS user_id,
       COALESCE(u.nombre_usuario, u.nombreUsuario, u.nombre_usu, u.nombre, u.nombre_usuario) AS nombre_usuario
FROM roles r
JOIN usuarios u ON u.role_id = r.id;

-- Usage: SELECT * FROM roles_with_users;
