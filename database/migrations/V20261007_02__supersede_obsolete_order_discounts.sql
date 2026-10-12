-- Un descuento deja de ser efectivo cuando una compensación posterior para el
-- mismo ítem termina en no cobrar o reposición. Conservamos el historial,
-- pero marcamos el descuento como sustituido para que no vuelva a aplicarse.
UPDATE order_item_incidents AS discount
SET status = 'SUPERSEDED'
WHERE discount.resolution = 'DISCOUNT'
  AND UPPER(TRIM(discount.status)) IN ('APPROVED', 'APROVED')
  AND EXISTS (
      SELECT 1
      FROM order_item_incidents AS compensation
      WHERE compensation.order_id = discount.order_id
        AND compensation.order_item_id = discount.order_item_id
        AND compensation.resolution IN ('CANCEL_NO_CHARGE', 'REPLACE')
        AND UPPER(TRIM(compensation.status)) IN ('APPROVED', 'APROVED')
        AND (
            compensation.reported_at > discount.reported_at
            OR (
                compensation.reported_at = discount.reported_at
                AND compensation.id > discount.id
            )
        )
  );
