-- Cargos monetarios configurables para envases y delivery. Las órdenes los
-- conservan antes del cobro y las ventas guardan el importe final cobrado.
ALTER TABLE orders
    ADD COLUMN IF NOT EXISTS packaging_charge NUMERIC(12,2) NOT NULL DEFAULT 0.00,
    ADD COLUMN IF NOT EXISTS delivery_charge NUMERIC(12,2) NOT NULL DEFAULT 0.00;

ALTER TABLE sales
    ADD COLUMN IF NOT EXISTS packaging_charge NUMERIC(12,2) NOT NULL DEFAULT 0.00,
    ADD COLUMN IF NOT EXISTS delivery_charge NUMERIC(12,2) NOT NULL DEFAULT 0.00;

-- También deja la migración preparada para una base que hubiera recibido las
-- columnas previamente sin sus restricciones definitivas.
UPDATE orders
SET packaging_charge = 0.00
WHERE packaging_charge IS NULL;

UPDATE orders
SET delivery_charge = 0.00
WHERE delivery_charge IS NULL;

UPDATE sales
SET packaging_charge = 0.00
WHERE packaging_charge IS NULL;

UPDATE sales
SET delivery_charge = 0.00
WHERE delivery_charge IS NULL;

ALTER TABLE orders
    ALTER COLUMN packaging_charge SET DEFAULT 0.00,
    ALTER COLUMN packaging_charge SET NOT NULL,
    ALTER COLUMN delivery_charge SET DEFAULT 0.00,
    ALTER COLUMN delivery_charge SET NOT NULL;

ALTER TABLE sales
    ALTER COLUMN packaging_charge SET DEFAULT 0.00,
    ALTER COLUMN packaging_charge SET NOT NULL,
    ALTER COLUMN delivery_charge SET DEFAULT 0.00,
    ALTER COLUMN delivery_charge SET NOT NULL;

DO $$
BEGIN
    IF NOT EXISTS (
        SELECT 1
        FROM pg_constraint
        WHERE conname = 'chk_orders_packaging_charge_non_negative'
          AND conrelid = 'orders'::regclass
    ) THEN
        ALTER TABLE orders
            ADD CONSTRAINT chk_orders_packaging_charge_non_negative
            CHECK (packaging_charge >= 0);
    END IF;

    IF NOT EXISTS (
        SELECT 1
        FROM pg_constraint
        WHERE conname = 'chk_orders_delivery_charge_non_negative'
          AND conrelid = 'orders'::regclass
    ) THEN
        ALTER TABLE orders
            ADD CONSTRAINT chk_orders_delivery_charge_non_negative
            CHECK (delivery_charge >= 0);
    END IF;

    IF NOT EXISTS (
        SELECT 1
        FROM pg_constraint
        WHERE conname = 'chk_sales_packaging_charge_non_negative'
          AND conrelid = 'sales'::regclass
    ) THEN
        ALTER TABLE sales
            ADD CONSTRAINT chk_sales_packaging_charge_non_negative
            CHECK (packaging_charge >= 0);
    END IF;

    IF NOT EXISTS (
        SELECT 1
        FROM pg_constraint
        WHERE conname = 'chk_sales_delivery_charge_non_negative'
          AND conrelid = 'sales'::regclass
    ) THEN
        ALTER TABLE sales
            ADD CONSTRAINT chk_sales_delivery_charge_non_negative
            CHECK (delivery_charge >= 0);
    END IF;

END $$;

COMMENT ON COLUMN orders.packaging_charge IS
    'Monto adicional por envases aplicado a la orden. Inicia en 0.00 y solo corresponde cuando se cobra un pedido para llevar o delivery.';
COMMENT ON COLUMN orders.delivery_charge IS
    'Monto adicional por el servicio de delivery aplicado a la orden. Inicia en 0.00 y solo corresponde cuando se cobra un pedido con entrega a domicilio.';
COMMENT ON COLUMN sales.packaging_charge IS
    'Monto final cobrado por envases. Inicia en 0.00 y se conserva como parte del historial financiero de la venta.';
COMMENT ON COLUMN sales.delivery_charge IS
    'Monto final cobrado por delivery. Inicia en 0.00 y se conserva como parte del historial financiero de la venta.';

-- La modalidad pertenece a la orden. Las ventas agrupan sus ítems por
-- producto y precio, por lo que se elimina de forma segura si esta migración
-- fue ejecutada antes de esa corrección.
ALTER TABLE sale_items
    DROP CONSTRAINT IF EXISTS chk_sale_items_fulfillment_type;

ALTER TABLE sale_items
    DROP COLUMN IF EXISTS fulfillment_type;
