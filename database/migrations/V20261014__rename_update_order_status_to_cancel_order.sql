-- UPDATE_ORDER_STATUS nunca se utiliza para el flujo de cocina ni para los
-- estados de productos. Se normaliza a CANCEL_ORDER, exclusivo para cancelar
-- una orden completa antes del cobro.
DO $$
DECLARE
    legacy_permission_id BIGINT;
    cancel_permission_id BIGINT;
BEGIN
    SELECT id INTO legacy_permission_id
    FROM permissions
    WHERE name = 'UPDATE_ORDER_STATUS';

    SELECT id INTO cancel_permission_id
    FROM permissions
    WHERE name = 'CANCEL_ORDER';

    IF legacy_permission_id IS NOT NULL AND cancel_permission_id IS NULL THEN
        UPDATE permissions
        SET name = 'CANCEL_ORDER',
            description = 'Permiso para cancelar una orden completa antes de cobrarla',
            modified_at = NOW()
        WHERE id = legacy_permission_id;
    ELSIF legacy_permission_id IS NOT NULL AND cancel_permission_id IS NOT NULL THEN
        INSERT INTO role_permissions (role_id, permission_id, created_by)
        SELECT role_id, cancel_permission_id, created_by
        FROM role_permissions
        WHERE permission_id = legacy_permission_id
        ON CONFLICT (role_id, permission_id) DO NOTHING;

        DELETE FROM role_permissions WHERE permission_id = legacy_permission_id;
        DELETE FROM permissions WHERE id = legacy_permission_id;
    END IF;

    UPDATE permissions
    SET description = 'Permiso para cancelar una orden completa antes de cobrarla',
        module = 'Operaciones Órdenes',
        modified_at = NOW()
    WHERE name = 'CANCEL_ORDER';
END $$;
