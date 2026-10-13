-- VIEW_DASHBOARD conserva el alcance general para los roles existentes.
-- VIEW_OWN_DASHBOARD permite restringir un rol a sus propias ventas.
INSERT INTO permissions (name, description, created_by, module)
VALUES
    ('VIEW_OWN_DASHBOARD', 'Permiso para consultar únicamente las ventas propias', 1, 'Inicio')
ON CONFLICT (name) DO NOTHING;

-- El administrador principal recibe el permiso de alcance personal.
INSERT INTO role_permissions (role_id, permission_id, created_by)
SELECT r.id, p.id, 1
FROM roles r
JOIN permissions p ON p.name = 'VIEW_OWN_DASHBOARD'
WHERE r.name = 'SUPER_ADMIN'
ON CONFLICT (role_id, permission_id) DO NOTHING;
