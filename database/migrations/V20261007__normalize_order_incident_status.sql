-- El estado correcto es APPROVED (doble P). Conserva la compatibilidad de
-- registros históricos que se almacenaron con el error tipográfico APROVED.
UPDATE order_item_incidents
SET status = 'APPROVED'
WHERE UPPER(TRIM(status)) = 'APROVED';
