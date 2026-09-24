-- ==========================================
-- Eliminamos primero tablas y secuencias por dependencias
-- ==========================================

DROP TABLE IF EXISTS role_permissions CASCADE;
DROP TABLE IF EXISTS user_roles CASCADE;
DROP TABLE IF EXISTS product_images CASCADE;
DROP TABLE IF EXISTS catalog_configs CASCADE;
DROP TABLE IF EXISTS products CASCADE;
DROP TABLE IF EXISTS permissions CASCADE;
DROP TABLE IF EXISTS roles CASCADE;
DROP TABLE IF EXISTS users CASCADE;
DROP TABLE IF EXISTS persons CASCADE;
DROP TABLE IF EXISTS parameters CASCADE;
DROP TABLE IF EXISTS inventory_movements CASCADE;
DROP TABLE IF EXISTS cash_registers CASCADE;
DROP TABLE IF EXISTS cash_sessions CASCADE;
DROP TABLE IF EXISTS orders CASCADE;
DROP TABLE IF EXISTS order_items CASCADE;
DROP TABLE IF EXISTS sales CASCADE;
DROP TABLE IF EXISTS sale_items CASCADE;
DROP TABLE IF EXISTS payments CASCADE;
DROP TABLE IF EXISTS inventory_count_sessions CASCADE;
DROP TABLE IF EXISTS inventory_count_items CASCADE;
DROP TABLE IF EXISTS user_notifications CASCADE;
DROP TABLE IF EXISTS notifications CASCADE;
DROP TABLE IF EXISTS sale_cancellations CASCADE;


-- ==========================================
-- Eliminamos secuencias
-- ==========================================

DROP SEQUENCE IF EXISTS roles_seq CASCADE;
DROP SEQUENCE IF EXISTS permissions_seq CASCADE;
DROP SEQUENCE IF EXISTS users_seq CASCADE;
DROP SEQUENCE IF EXISTS persons_seq CASCADE;
DROP SEQUENCE IF EXISTS parameters_seq CASCADE;
DROP SEQUENCE IF EXISTS user_roles_seq CASCADE;
DROP SEQUENCE IF EXISTS products_seq CASCADE;
DROP SEQUENCE IF EXISTS product_images_seq CASCADE;
DROP SEQUENCE IF EXISTS catalog_configs_seq CASCADE;
DROP SEQUENCE IF EXISTS inventory_movements_seq CASCADE;
DROP SEQUENCE IF EXISTS cash_registers_seq CASCADE;
DROP SEQUENCE IF EXISTS cash_sessions_seq CASCADE;
DROP SEQUENCE IF EXISTS orders_seq CASCADE;
DROP SEQUENCE IF EXISTS order_items_seq CASCADE;
DROP SEQUENCE IF EXISTS sales_seq CASCADE;
DROP SEQUENCE IF EXISTS sale_items_seq CASCADE;
DROP SEQUENCE IF EXISTS payments_seq CASCADE;
DROP SEQUENCE IF EXISTS inventory_count_sessions_seq CASCADE;
DROP SEQUENCE IF EXISTS inventory_count_items_seq CASCADE;
DROP SEQUENCE IF EXISTS user_notifications_seq CASCADE;
DROP SEQUENCE IF EXISTS notifications_seq CASCADE;
DROP SEQUENCE IF EXISTS sale_cancellations_seq CASCADE;

-- ==========================================
-- Secuencias para tablas
-- ==========================================
CREATE SEQUENCE persons_seq START 1 INCREMENT 1;
CREATE SEQUENCE users_seq START 1 INCREMENT 1;
CREATE SEQUENCE roles_seq START 1 INCREMENT 1;
CREATE SEQUENCE permissions_seq START 1 INCREMENT 1;
CREATE SEQUENCE parameters_seq START 1 INCREMENT 1;
CREATE SEQUENCE user_roles_seq START 1 INCREMENT 1;
CREATE SEQUENCE products_seq START 1 INCREMENT 1;
CREATE SEQUENCE product_images_seq START 1 INCREMENT 1;
CREATE SEQUENCE catalog_configs_seq START 1 INCREMENT 1;
CREATE sequence inventory_movements_seq START 1 INCREMENT 1;
CREATE SEQUENCE cash_registers_seq START 1 INCREMENT 1;
CREATE SEQUENCE cash_sessions_seq START 1 INCREMENT 1;
CREATE SEQUENCE orders_seq START 1 INCREMENT 1;
CREATE SEQUENCE order_items_seq START 1 INCREMENT 1;
CREATE SEQUENCE sales_seq START 1 INCREMENT 1;
CREATE SEQUENCE sale_items_seq START 1 INCREMENT 1;
CREATE SEQUENCE payments_seq START 1 INCREMENT 1;
CREATE SEQUENCE inventory_count_sessions_seq START 1 INCREMENT 1;
CREATE SEQUENCE inventory_count_items_seq START 1 INCREMENT 1;
CREATE SEQUENCE notifications_seq START 1 INCREMENT 1;
CREATE SEQUENCE user_notifications_seq START 1 INCREMENT 1;
CREATE SEQUENCE IF NOT EXISTS sale_cancellations_seq START 1 INCREMENT 1;


-- ==========================================
-- Tabla de personas
-- ==========================================
CREATE TABLE persons (
    id BIGINT PRIMARY KEY DEFAULT nextval('persons_seq'),
    type_document BIGINT NULL,
    document VARCHAR(20),
    full_name VARCHAR(255) NOT NULL,
    phone VARCHAR(50),
    address VARCHAR(255),

    created_by BIGINT,
    created_at TIMESTAMP DEFAULT now(),
    modified_by BIGINT,
    modified_at TIMESTAMP,
    deleted_by BIGINT,
    deleted_at TIMESTAMP
);

-- ==========================================
-- Tabla de usuarios
-- ==========================================
CREATE TABLE users (
    id BIGINT PRIMARY KEY DEFAULT nextval('users_seq'),
    person_id BIGINT NOT NULL REFERENCES persons(id),
    username VARCHAR(50) NOT NULL,
    password VARCHAR(255) NOT NULL,
    email VARCHAR(100) NOT NULL,
    avatar_url VARCHAR(500),
    active BOOLEAN NOT NULL DEFAULT TRUE,

    created_by BIGINT,
    created_at TIMESTAMP DEFAULT now(),
    modified_by BIGINT,
    modified_at TIMESTAMP,
    deleted_by BIGINT,
    deleted_at TIMESTAMP
);

-- ==========================================
-- Tabla de roles
-- ==========================================
CREATE TABLE roles (
    id BIGINT PRIMARY KEY DEFAULT nextval('roles_seq'),
    name VARCHAR(255) NOT NULL,
    description VARCHAR(255),
    active BOOLEAN NOT NULL DEFAULT TRUE,

    created_by BIGINT,
    created_at TIMESTAMP DEFAULT now(),
    modified_by BIGINT,
    modified_at TIMESTAMP,
    deleted_by BIGINT,
    deleted_at TIMESTAMP
);

-- ==========================================
-- Tabla de permisos
-- ==========================================
CREATE TABLE permissions (
    id BIGINT PRIMARY KEY DEFAULT nextval('permissions_seq'),
    name VARCHAR(100) NOT NULL UNIQUE,
    description VARCHAR(255),
    module VARCHAR(255),

    created_by BIGINT,
    created_at TIMESTAMP DEFAULT now(),
    modified_by BIGINT,
    modified_at TIMESTAMP,
    deleted_by BIGINT,
    deleted_at TIMESTAMP
);

-- ==========================================
-- Tabla intermedia usuario-roles (con PK propia)
-- ==========================================

CREATE TABLE user_roles (
    id BIGINT PRIMARY KEY DEFAULT nextval('user_roles_seq'),
    user_id BIGINT NOT NULL,
    role_id BIGINT NOT NULL,
    assigned_at TIMESTAMP DEFAULT now(),
    assigned_by BIGINT NULL,
    created_by BIGINT NULL,
    created_at TIMESTAMP NULL,
    modified_by BIGINT NULL,
    modified_at TIMESTAMP NULL,
    deleted_by BIGINT NULL,
    deleted_at TIMESTAMP NULL,


    CONSTRAINT fk_user_roles_user FOREIGN KEY (user_id) REFERENCES users(id) ON DELETE CASCADE,
    CONSTRAINT fk_user_roles_role FOREIGN KEY (role_id) REFERENCES roles(id) ON DELETE CASCADE,
    CONSTRAINT uq_user_role UNIQUE (user_id, role_id) -- evita duplicados
);

-- ==========================================
-- Tabla intermedia rol-permisos
-- ==========================================
CREATE TABLE role_permissions (
    role_id BIGINT NOT NULL,
    permission_id BIGINT NOT NULL,
    assigned_at TIMESTAMP DEFAULT now(),

    created_by BIGINT,
    created_at TIMESTAMP DEFAULT now(),
    modified_by BIGINT,
    modified_at TIMESTAMP,
    deleted_by BIGINT,
    deleted_at TIMESTAMP,

    PRIMARY KEY (role_id, permission_id),
    CONSTRAINT fk_role_permissions_role FOREIGN KEY (role_id) REFERENCES roles(id) ON DELETE CASCADE,
    CONSTRAINT fk_role_permissions_permission FOREIGN KEY (permission_id) REFERENCES permissions(id) ON DELETE CASCADE
);

-- ==========================================
-- Tabla de parámetros
-- ==========================================
CREATE TABLE parameters (
    id BIGINT PRIMARY KEY DEFAULT nextval('parameters_seq'),
    parent_parameter_id BIGINT NULL,
    parameter_id BIGINT NULL,
    code VARCHAR(255) NOT NULL,
    type BIGINT NULL,
    name VARCHAR(255) NOT NULL,
    short_name VARCHAR(255) NULL,
    order_number BIGINT NULL,
    active BOOLEAN NOT NULL DEFAULT TRUE,

    created_by BIGINT,
    created_at TIMESTAMP DEFAULT now(),
    modified_by BIGINT,
    modified_at TIMESTAMP,
    deleted_by BIGINT,
    deleted_at TIMESTAMP
);


-- ==========================================
-- Tabla de productos
-- ==========================================
CREATE TABLE products (
    id BIGINT PRIMARY KEY DEFAULT nextval('products_seq'),
    code VARCHAR(50) NOT NULL,
    name VARCHAR(255) NOT NULL,
    description TEXT,
    category_id BIGINT NOT NULL,
    unit_measure_id BIGINT NOT NULL,
    manage_variants BOOLEAN DEFAULT FALSE,
    valuation_method_id BIGINT NOT NULL DEFAULT 1,
    base_price NUMERIC(12,2) DEFAULT 0,
    promo_price NUMERIC(12,2) DEFAULT 0,
    base_cost NUMERIC(12,2) DEFAULT 0,
    total_stock BIGINT DEFAULT 0,
    reserved_stock BIGINT NOT NULL DEFAULT 0,
    active BOOLEAN DEFAULT TRUE,
    created_by BIGINT,
    created_at TIMESTAMP DEFAULT now(),
    modified_by BIGINT,
    modified_at TIMESTAMP,
    deleted_by BIGINT,
    deleted_at TIMESTAMP
);

-- Evita códigos duplicados sin distinguir mayúsculas, minúsculas o espacios externos.
CREATE UNIQUE INDEX uk_products_code_case_insensitive
ON products (LOWER(TRIM(code)));

-- ==========================================
-- Comentarios de la tabla y columnas
-- ==========================================
COMMENT ON TABLE products IS 'Tabla principal de productos. Contiene información de inventario, precios, variantes y auditoría.';

COMMENT ON COLUMN products.id IS 'Identificador único del producto.';
COMMENT ON COLUMN products.code IS 'Código único del producto (usado para búsquedas y control de inventario).';
COMMENT ON COLUMN products.name IS 'Nombre del producto.';
COMMENT ON COLUMN products.description IS 'Descripción opcional del producto.';
COMMENT ON COLUMN products.category_id IS 'Clave lógica (parámetro: CATEGORIA_PRODUCTO).';
COMMENT ON COLUMN products.unit_measure_id IS 'Clave lógica (parámetro: UNIDAD_MEDIDA).';
COMMENT ON COLUMN products.manage_variants IS 'Indica si el producto maneja variantes (color, talla, etc.).';
COMMENT ON COLUMN products.valuation_method_id IS 'Clave lógica (parámetro: METODO_VALORIZACION) - FIFO, LIFO o PROMEDIO.';
COMMENT ON COLUMN products.base_price IS 'Precio base estándar de venta.';
COMMENT ON COLUMN products.promo_price IS 'Precio promocional o con descuento.';
COMMENT ON COLUMN products.base_cost IS 'Costo unitario base (usado para calcular márgenes o ganancias).';
COMMENT ON COLUMN products.total_stock IS 'Stock total (suma de todas las variantes o stock único).';
COMMENT ON COLUMN products.reserved_stock is 'Cantidad de unidades comprometidas en órdenes activas y aún no descontadas del stock físico.';
COMMENT ON COLUMN products.active IS 'Estado lógico del producto (TRUE = activo, FALSE = eliminado lógicamente).';
COMMENT ON COLUMN products.created_by IS 'Usuario que creó el registro.';
COMMENT ON COLUMN products.created_at IS 'Fecha y hora de creación del registro.';
COMMENT ON COLUMN products.modified_by IS 'Usuario que realizó la última modificación.';
COMMENT ON COLUMN products.modified_at IS 'Fecha y hora de la última modificación.';
COMMENT ON COLUMN products.deleted_by IS 'Usuario que eliminó lógicamente el registro.';
COMMENT ON COLUMN products.deleted_at IS 'Fecha y hora de eliminación lógica.';


-- ==========================================
-- Tabla de imagenes de productos
-- ==========================================
CREATE TABLE product_images (
    id BIGINT PRIMARY KEY DEFAULT nextval('product_images_seq'),
    product_id BIGINT NOT NULL,
    image_url VARCHAR(1000) NOT NULL,
    alt_text VARCHAR(255),
    is_main BOOLEAN DEFAULT FALSE,
    order_number INT DEFAULT 1,
    active BOOLEAN DEFAULT TRUE,

    created_by BIGINT,
    created_at TIMESTAMP DEFAULT now(),
    modified_by BIGINT,
    modified_at TIMESTAMP,
    deleted_by BIGINT,
    deleted_at TIMESTAMP,

    CONSTRAINT fk_product_images_product
        FOREIGN KEY (product_id)
        REFERENCES products(id)
        ON DELETE CASCADE
);

-- ==========================================
-- Comentarios de la tabla y columnas
-- ==========================================
COMMENT ON TABLE product_images IS 'Imágenes asociadas a productos. Permite múltiples imágenes por producto, imagen principal y orden.';

COMMENT ON COLUMN product_images.id IS 'Identificador único de la imagen del producto.';
COMMENT ON COLUMN product_images.product_id IS 'Producto al que pertenece la imagen.';
COMMENT ON COLUMN product_images.image_url IS 'URL pública de la imagen (S3, Cloudinary, servidor local, etc.).';
COMMENT ON COLUMN product_images.alt_text IS 'Texto alternativo para accesibilidad y SEO.';
COMMENT ON COLUMN product_images.is_main IS 'Indica si es la imagen principal del producto.';
COMMENT ON COLUMN product_images.order_number IS 'Orden de visualización de la imagen.';
COMMENT ON COLUMN product_images.active IS 'Estado lógico de la imagen.';
COMMENT ON COLUMN product_images.created_by IS 'Usuario que creó el registro.';
COMMENT ON COLUMN product_images.created_at IS 'Fecha de creación.';
COMMENT ON COLUMN product_images.modified_by IS 'Usuario que modificó el registro.';
COMMENT ON COLUMN product_images.modified_at IS 'Fecha de modificación.';
COMMENT ON COLUMN product_images.deleted_by IS 'Usuario que eliminó lógicamente el registro.';
COMMENT ON COLUMN product_images.deleted_at IS 'Fecha de eliminación lógica.';


-- ==========================================
-- Tabla de configuracion de catalogo
-- ==========================================
CREATE TABLE catalog_configs (
    id BIGINT PRIMARY KEY DEFAULT nextval('catalog_configs_seq'),

    name VARCHAR(100) NOT NULL,

    -- PORTADA
    show_cover BOOLEAN DEFAULT TRUE,
    cover_image VARCHAR(500),
    header_image VARCHAR(500),

    -- VISTA PRODUCTOS
    view_mode VARCHAR(20) DEFAULT 'GRID',
    columns INT DEFAULT 3,

    show_image BOOLEAN DEFAULT TRUE,
    show_price BOOLEAN DEFAULT TRUE,
    show_price_promo BOOLEAN DEFAULT FALSE,
    show_description BOOLEAN DEFAULT TRUE,
    show_code BOOLEAN DEFAULT TRUE,
    card_border BOOLEAN DEFAULT FALSE,

    -- PAGINACIÓN
    products_per_page INT DEFAULT 12,
    show_page_number BOOLEAN DEFAULT TRUE,
    show_header BOOLEAN DEFAULT TRUE,
    show_footer BOOLEAN DEFAULT TRUE,

    show_status BOOLEAN DEFAULT TRUE,

    active BOOLEAN DEFAULT TRUE,

    created_by BIGINT,
    created_at TIMESTAMP DEFAULT now(),
    modified_by BIGINT,
    modified_at TIMESTAMP,
    deleted_by BIGINT,
    deleted_at TIMESTAMP
);

-- ==========================================
-- Comentarios de la tabla y columnas
-- ==========================================

COMMENT ON TABLE catalog_configs IS 'Configuración del catálogo de productos. Define cómo se mostrarán los productos al generar el catálogo, incluyendo portada, layout, visibilidad de información y opciones de paginación.';
COMMENT ON COLUMN catalog_configs.id IS 'Identificador único de la configuración del catálogo.';
COMMENT ON COLUMN catalog_configs.name IS 'Nombre descriptivo de la configuración del catálogo (ejemplo: Catálogo 2026, Catálogo Promocional, Catálogo Básico).';
COMMENT ON COLUMN catalog_configs.show_cover IS 'Indica si el catálogo generado incluirá una página de portada.';
COMMENT ON COLUMN catalog_configs.cover_image IS 'URL o ruta de la imagen utilizada como portada del catálogo.';
COMMENT ON COLUMN catalog_configs.view_mode IS 'Modo de visualización de los productos en el catálogo (GRID = mosaico, LIST = lista).';
COMMENT ON COLUMN catalog_configs.columns IS 'Número de columnas que se utilizarán para mostrar los productos cuando el modo de visualización sea GRID.';
COMMENT ON COLUMN catalog_configs.show_image IS 'Indica si se debe mostrar la imagen principal del producto dentro del catálogo.';
COMMENT ON COLUMN catalog_configs.show_price IS 'Indica si se debe mostrar el precio base del producto en el catálogo.';
COMMENT ON COLUMN catalog_configs.show_price_promo IS 'Indica si se debe mostrar el precio promocional del producto en el catálogo.';
COMMENT ON COLUMN catalog_configs.show_description IS 'Indica si se debe mostrar la descripción del producto dentro del catálogo.';
COMMENT ON COLUMN catalog_configs.show_code IS 'Indica si se debe mostrar el código del producto dentro del catálogo.';
COMMENT ON COLUMN catalog_configs.products_per_page IS 'Cantidad máxima de productos que se mostrarán por página en el catálogo generado.';
COMMENT ON COLUMN catalog_configs.show_page_number IS 'Indica si se debe mostrar el número de página en el catálogo.';
COMMENT ON COLUMN catalog_configs.show_header IS 'Indica si el catálogo incluirá un encabezado en cada página.';
COMMENT ON COLUMN catalog_configs.show_footer IS 'Indica si el catálogo incluirá un pie de página en cada página.';
COMMENT ON COLUMN catalog_configs.active IS 'Estado lógico de la configuración del catálogo (TRUE = activo, FALSE = inactivo).';
COMMENT ON COLUMN catalog_configs.created_by IS 'Usuario que creó el registro de configuración del catálogo.';
COMMENT ON COLUMN catalog_configs.created_at IS 'Fecha y hora en que se creó la configuración del catálogo.';
COMMENT ON COLUMN catalog_configs.modified_by IS 'Usuario que realizó la última modificación del registro.';
COMMENT ON COLUMN catalog_configs.modified_at IS 'Fecha y hora de la última modificación del registro.';
COMMENT ON COLUMN catalog_configs.deleted_by IS 'Usuario que eliminó lógicamente la configuración del catálogo.';
COMMENT ON COLUMN catalog_configs.deleted_at IS 'Fecha y hora de eliminación lógica del registro.';


-- ==========================================
-- Tabla de movimientos de inventario
-- ==========================================
CREATE TABLE inventory_movements (
    id BIGINT PRIMARY KEY DEFAULT nextval('inventory_movements_seq'),
    product_id BIGINT NOT NULL,
    type VARCHAR(30) NOT NULL,
    quantity NUMERIC(12,2) NOT NULL,
    previous_stock NUMERIC(12,2) NOT NULL,
    current_stock NUMERIC(12,2) NOT NULL,
    reason VARCHAR(255),
    reference_type VARCHAR(30),
    reference_id BIGINT,
    created_by BIGINT,
    created_at TIMESTAMP DEFAULT now(),
    modified_by BIGINT,
    modified_at TIMESTAMP,
    deleted_by BIGINT,
    deleted_at TIMESTAMP
);

-- ==========================================
-- Comentarios de la tabla y columnas
-- ==========================================

COMMENT ON TABLE inventory_movements IS 'Historial de movimientos de inventario realizados sobre los productos. Registra ingresos, ventas, mermas, ajustes y cualquier operación que modifique el stock disponible.';
COMMENT ON COLUMN inventory_movements.id IS 'Identificador único del movimiento de inventario.';
COMMENT ON COLUMN inventory_movements.product_id IS 'Identificador del producto afectado por el movimiento de inventario.';
COMMENT ON COLUMN inventory_movements.type IS 'Tipo de movimiento realizado sobre el producto (ENTRY, SALE, WASTE, ADJUSTMENT, RETURN, entre otros).';
COMMENT ON COLUMN inventory_movements.quantity IS 'Cantidad de unidades involucradas en el movimiento de inventario.';
COMMENT ON COLUMN inventory_movements.previous_stock IS 'Cantidad de stock disponible antes de realizar el movimiento.';
COMMENT ON COLUMN inventory_movements.current_stock IS 'Cantidad de stock disponible después de aplicar el movimiento.';
COMMENT ON COLUMN inventory_movements.reason IS 'Observación o motivo del movimiento de inventario. Se utiliza principalmente para registrar mermas o ajustes manuales.';
COMMENT ON COLUMN inventory_movements.reference_type IS 'Tipo de operación que originó el movimiento (SALE, PURCHASE, WASTE, ADJUSTMENT, entre otros).';
COMMENT ON COLUMN inventory_movements.reference_id IS 'Identificador del registro relacionado con el movimiento, como una venta, compra o ajuste.';
COMMENT ON COLUMN inventory_movements.created_by IS 'Usuario que registró el movimiento de inventario.';
COMMENT ON COLUMN inventory_movements.created_at IS 'Fecha y hora en que se registró el movimiento de inventario.';
COMMENT ON COLUMN inventory_movements.modified_by IS 'Usuario que realizó la última modificación del movimiento de inventario.';
COMMENT ON COLUMN inventory_movements.modified_at IS 'Fecha y hora de la última modificación del movimiento.';
COMMENT ON COLUMN inventory_movements.deleted_by IS 'Usuario que realizó la eliminación lógica del movimiento de inventario.';
COMMENT ON COLUMN inventory_movements.deleted_at IS 'Fecha y hora en que se realizó la eliminación lógica del movimiento.';


-- ==========================================
-- Tabla de cajas
-- ==========================================

CREATE TABLE cash_registers (
    id BIGINT PRIMARY KEY DEFAULT nextval('cash_registers_seq'),
    code VARCHAR(30) NOT NULL UNIQUE,
    name VARCHAR(100) NOT NULL,
    description VARCHAR(255),
    active BOOLEAN NOT NULL DEFAULT TRUE,
    created_by BIGINT,
    created_at TIMESTAMP DEFAULT now(),
    modified_by BIGINT,
    modified_at TIMESTAMP,
    deleted_by BIGINT,
    deleted_at TIMESTAMP
);

-- ==========================================
-- Comentarios de la tabla y columnas
-- ==========================================

COMMENT ON TABLE cash_registers IS 'Tabla de cajas registradoras del sistema. Representa las cajas físicas o puntos de cobro disponibles para realizar operaciones de venta y controlar el dinero.';
COMMENT ON COLUMN cash_registers.id IS 'Identificador único de la caja registradora.';
COMMENT ON COLUMN cash_registers.code IS 'Código único de identificación de la caja registradora. Ejemplo: CAJA-01, CAJA-02.';
COMMENT ON COLUMN cash_registers.name IS 'Nombre descriptivo de la caja registradora. Ejemplo: Caja Principal, Caja Secundaria.';
COMMENT ON COLUMN cash_registers.description IS 'Descripción opcional de la caja registradora y su ubicación o función.';
COMMENT ON COLUMN cash_registers.active IS 'Indica si la caja se encuentra habilitada para operar. TRUE = activa, FALSE = inactiva.';
COMMENT ON COLUMN cash_registers.created_by IS 'Usuario que creó el registro de la caja.';
COMMENT ON COLUMN cash_registers.created_at IS 'Fecha y hora en que se creó el registro de la caja.';
COMMENT ON COLUMN cash_registers.modified_by IS 'Usuario que realizó la última modificación de la caja.';
COMMENT ON COLUMN cash_registers.modified_at IS 'Fecha y hora de la última modificación de la caja.';
COMMENT ON COLUMN cash_registers.deleted_by IS 'Usuario que realizó la eliminación lógica de la caja.';
COMMENT ON COLUMN cash_registers.deleted_at IS 'Fecha y hora en que se realizó la eliminación lógica de la caja.';


-- ==========================================
-- Tabla de sesiones de caja
-- ==========================================

CREATE TABLE cash_sessions (
    id BIGINT PRIMARY KEY DEFAULT nextval('cash_sessions_seq'),
    cash_register_id BIGINT NOT NULL,
    opened_by BIGINT NOT NULL,
    opened_at TIMESTAMP NOT NULL DEFAULT NOW(),
    opening_amount NUMERIC(12,2) NOT NULL DEFAULT 0,
    closed_by BIGINT,
    closed_at TIMESTAMP,
    expected_amount NUMERIC(12,2),
    closing_amount NUMERIC(12,2),
    difference NUMERIC(12,2),
    status VARCHAR(20) NOT NULL DEFAULT 'OPEN',
    opening_comment VARCHAR(255),
    closing_comment VARCHAR(255),
    created_by BIGINT,
    created_at TIMESTAMP DEFAULT NOW(),
    modified_by BIGINT,
    modified_at TIMESTAMP,
    deleted_by BIGINT,
    deleted_at TIMESTAMP,

    CONSTRAINT fk_cash_sessions_register
        FOREIGN KEY (cash_register_id)
        REFERENCES cash_registers(id),

    CONSTRAINT fk_cash_sessions_opened_by
        FOREIGN KEY (opened_by)
        REFERENCES users(id),

    CONSTRAINT fk_cash_sessions_closed_by
        FOREIGN KEY (closed_by)
        REFERENCES users(id)
);

-- ==========================================
-- Comentarios de la tabla y columnas
-- ==========================================

COMMENT ON TABLE cash_sessions is 'Tabla que registra las sesiones de caja. Cada sesión representa un período de operación de una caja, desde su apertura hasta su cierre, incluyendo el monto inicial, monto esperado, monto contado y diferencia.';
COMMENT ON COLUMN cash_sessions.id is 'Identificador único de la sesión de caja.';
COMMENT ON COLUMN cash_sessions.cash_register_id is 'Identificador de la caja registradora a la que pertenece la sesión.';
COMMENT ON COLUMN cash_sessions.opened_by is 'Usuario que realizó la apertura de la sesión de caja.';
COMMENT ON COLUMN cash_sessions.opened_at is 'Fecha y hora en que se realizó la apertura de la sesión de caja.';
COMMENT ON COLUMN cash_sessions.opening_amount is 'Monto de dinero disponible en la caja al momento de realizar la apertura.';
COMMENT ON COLUMN cash_sessions.closed_by is 'Usuario que realizó el cierre de la sesión de caja.';
COMMENT ON COLUMN cash_sessions.closed_at is 'Fecha y hora en que se realizó el cierre de la sesión de caja.';
COMMENT ON COLUMN cash_sessions.expected_amount is 'Monto que el sistema calcula que debería existir en la caja al momento del cierre, considerando apertura, ventas y movimientos de efectivo.';
COMMENT ON COLUMN cash_sessions.closing_amount is 'Monto de dinero contado físicamente en la caja al momento del cierre.';
COMMENT ON COLUMN cash_sessions.difference is 'Diferencia entre el monto contado físicamente y el monto esperado por el sistema. Un valor negativo representa un faltante y un valor positivo representa un sobrante.';
COMMENT ON COLUMN cash_sessions.status is 'Estado de la sesión de caja. OPEN = sesión abierta, CLOSED = sesión cerrada.';
COMMENT ON COLUMN cash_sessions.opening_comment is 'Comentario u observación registrada durante la apertura de la caja.';
COMMENT ON COLUMN cash_sessions.closing_comment is 'Comentario u observación registrada durante el cierre de la caja.';
COMMENT ON COLUMN cash_sessions.created_by is 'Usuario que creó el registro de la sesión de caja.';
COMMENT ON COLUMN cash_sessions.created_at is 'Fecha y hora en que se creó el registro de la sesión de caja.';
COMMENT ON COLUMN cash_sessions.modified_by is 'Usuario que realizó la última modificación de la sesión de caja.';
COMMENT ON COLUMN cash_sessions.modified_at is 'Fecha y hora de la última modificación de la sesión de caja.';
COMMENT ON COLUMN cash_sessions.deleted_by is 'Usuario que realizó la eliminación lógica de la sesión de caja.';
COMMENT ON COLUMN cash_sessions.deleted_at is 'Fecha y hora en que se realizó la eliminación lógica de la sesión de caja.';


-- ==========================================
-- Tabla de ordenes
-- ==========================================
CREATE TABLE orders (
    id BIGINT PRIMARY KEY DEFAULT nextval('orders_seq'),
    order_number VARCHAR(30) NOT NULL UNIQUE,
    cash_register_id BIGINT,
    order_type VARCHAR(20) NOT NULL DEFAULT 'DINE_IN',
    table_number VARCHAR(20),
    status VARCHAR(20) NOT NULL DEFAULT 'PENDING',
    notes VARCHAR(500),
    created_by BIGINT,
    created_at TIMESTAMP DEFAULT NOW(),
    modified_by BIGINT,
    modified_at TIMESTAMP,
    deleted_by BIGINT,
    deleted_at TIMESTAMP,

    CONSTRAINT fk_orders_cash_register
        FOREIGN KEY (cash_register_id)
        REFERENCES cash_registers(id)
);

-- ==========================================
-- Comentarios de la tabla orders
-- ==========================================

COMMENT ON TABLE orders is 'Tabla de órdenes de atención. Representa una solicitud o pedido realizado por un cliente antes de convertirse en una venta.';
COMMENT ON COLUMN orders.id is 'Identificador único de la orden.';
COMMENT ON COLUMN orders.order_number is 'Número único de identificación de la orden.';
COMMENT ON COLUMN orders.cash_register_id is 'Caja registradora asociada a la orden cuando la operación se encuentra vinculada a una caja.';
COMMENT ON COLUMN orders.order_type is 'Tipo de atención de la orden. Ejemplos: DINE_IN = consumo en salón, TAKEAWAY = para llevar, DELIVERY = delivery.';
COMMENT ON COLUMN orders.table_number is 'Número o identificador de la mesa asociada a la orden cuando corresponde a una atención en salón.';
COMMENT ON COLUMN orders.status is 'Estado actual de la orden. Ejemplos: PENDING = pendiente, PREPARING = en preparación, READY = lista, COMPLETED = completada, CANCELLED = cancelada.';
COMMENT ON COLUMN orders.notes is 'Observaciones generales asociadas a la orden.';
COMMENT ON COLUMN orders.created_by is 'Usuario que creó la orden.';
COMMENT ON COLUMN orders.created_at is 'Fecha y hora en que se creó la orden.';
COMMENT ON COLUMN orders.modified_by is 'Usuario que realizó la última modificación de la orden.';
COMMENT ON COLUMN orders.modified_at is 'Fecha y hora de la última modificación de la orden.';
COMMENT ON COLUMN orders.deleted_by is 'Usuario que realizó la eliminación lógica de la orden.';
COMMENT ON COLUMN orders.deleted_at is 'Fecha y hora en que se realizó la eliminación lógica de la orden.';


-- ==========================================
-- Tabla de items de la orden
-- ==========================================
CREATE TABLE order_items (
    id BIGINT PRIMARY KEY DEFAULT nextval('order_items_seq'),
    order_id BIGINT NOT NULL,
    product_id BIGINT NOT NULL,
    quantity NUMERIC(12,2) NOT NULL,
    unit_price NUMERIC(12,2) NOT NULL,
    notes VARCHAR(255),
    created_at TIMESTAMP DEFAULT NOW(),

    CONSTRAINT fk_order_items_order
        FOREIGN KEY (order_id)
        REFERENCES orders(id)
        ON DELETE CASCADE,

    CONSTRAINT fk_order_items_product
        FOREIGN KEY (product_id)
        REFERENCES products(id)
);

-- ==========================================
-- Comentarios de la tabla order_items
-- ==========================================

COMMENT ON TABLE order_items is 'Detalle de productos incluidos en una orden. Cada registro representa un producto y la cantidad solicitada dentro de una orden.';
COMMENT ON COLUMN order_items.id is 'Identificador único del detalle de la orden.';
COMMENT ON COLUMN order_items.order_id is 'Identificador de la orden a la que pertenece el detalle.';
COMMENT ON COLUMN order_items.product_id is 'Identificador del producto solicitado en la orden.';
COMMENT ON COLUMN order_items.quantity is 'Cantidad del producto solicitada en la orden.';
COMMENT ON COLUMN order_items.unit_price is 'Precio unitario del producto registrado al momento de crear la orden.';
COMMENT ON COLUMN order_items.notes is 'Observaciones específicas del producto dentro de la orden.';
COMMENT ON COLUMN order_items.created_at is 'Fecha y hora en que se agregó el producto a la orden.';


-- ==========================================
-- Tabla de ventas
-- ==========================================
CREATE TABLE sales (
    id BIGINT PRIMARY KEY DEFAULT nextval('sales_seq'),
    sale_number VARCHAR(30) NOT NULL UNIQUE,
    order_id BIGINT NULL,
    cash_register_id BIGINT NOT NULL,
    subtotal NUMERIC(12,2) NOT NULL DEFAULT 0,
    discount NUMERIC(12,2) NOT NULL DEFAULT 0,
    total NUMERIC(12,2) NOT NULL DEFAULT 0,
    status VARCHAR(20) NOT NULL DEFAULT 'COMPLETED',
    created_by BIGINT,
    created_at TIMESTAMP DEFAULT NOW(),
    modified_by BIGINT,
    modified_at TIMESTAMP,
    deleted_by BIGINT,
    deleted_at TIMESTAMP,

    CONSTRAINT fk_sales_order
        FOREIGN KEY (order_id)
        REFERENCES orders(id),

    CONSTRAINT fk_sales_cash_register
        FOREIGN KEY (cash_register_id)
        REFERENCES cash_registers(id)
);

-- ==========================================
-- Comentarios de la tabla sales
-- ==========================================

COMMENT ON TABLE sales is 'Tabla principal de ventas realizadas. Representa la operación comercial generada a partir de una orden o directamente desde el punto de venta.';
COMMENT ON COLUMN sales.id is 'Identificador único de la venta.';
COMMENT ON COLUMN sales.sale_number is 'Número único de identificación de la venta.';
COMMENT ON COLUMN sales.order_id is 'Identificador de la orden que originó la venta. Puede ser NULL cuando la venta se realiza directamente sin una orden previa.';
COMMENT ON COLUMN sales.cash_register_id is 'Identificador de la caja registradora en la que se realizó la venta.';
COMMENT ON COLUMN sales.subtotal is 'Subtotal de la venta antes de aplicar descuentos.';
COMMENT ON COLUMN sales.discount is 'Monto total de descuento aplicado a la venta.';
COMMENT ON COLUMN sales.total is 'Importe total final de la venta después de aplicar descuentos.';
COMMENT ON COLUMN sales.status is 'Estado de la venta. Ejemplos: COMPLETED = completada, CANCELLED = cancelada, REFUNDED = devuelta o anulada.';
COMMENT ON COLUMN sales.created_by is 'Usuario que registró la venta.';
COMMENT ON COLUMN sales.created_at is 'Fecha y hora en que se registró la venta.';
COMMENT ON COLUMN sales.modified_by is 'Usuario que realizó la última modificación de la venta.';
COMMENT ON COLUMN sales.modified_at is 'Fecha y hora de la última modificación de la venta.';
COMMENT ON COLUMN sales.deleted_by is 'Usuario que realizó la eliminación lógica de la venta.';
COMMENT ON COLUMN sales.deleted_at is 'Fecha y hora en que se realizó la eliminación lógica de la venta.';


-- ==========================================
-- Tabla de items de la venta
-- ==========================================
CREATE TABLE sale_items (
    id BIGINT PRIMARY KEY DEFAULT nextval('sale_items_seq'),
    sale_id BIGINT NOT NULL,
    product_id BIGINT NOT NULL,
    quantity NUMERIC(12,2) NOT NULL,
    unit_price NUMERIC(12,2) NOT NULL,
    discount NUMERIC(12,2) NOT NULL DEFAULT 0,
    subtotal NUMERIC(12,2) NOT NULL DEFAULT 0,

    CONSTRAINT fk_sale_items_sale
        FOREIGN KEY (sale_id)
        REFERENCES sales(id)
        ON DELETE CASCADE,

    CONSTRAINT fk_sale_items_product
        FOREIGN KEY (product_id)
        REFERENCES products(id)
);

-- ==========================================
-- Comentarios de la tabla sale_items
-- ==========================================

COMMENT ON TABLE sale_items is 'Detalle de productos vendidos. Cada registro representa un producto, cantidad, precio y descuento aplicado dentro de una venta.';
COMMENT ON COLUMN sale_items.id is 'Identificador único del detalle de la venta.';
COMMENT ON COLUMN sale_items.sale_id is 'Identificador de la venta a la que pertenece el detalle.';
COMMENT ON COLUMN sale_items.product_id is 'Identificador del producto vendido.';
COMMENT ON COLUMN sale_items.quantity is 'Cantidad de unidades del producto vendidas.';
COMMENT ON COLUMN sale_items.unit_price is 'Precio unitario del producto aplicado en la venta.';
COMMENT ON COLUMN sale_items.discount is 'Monto de descuento aplicado específicamente al producto dentro de la venta.';
COMMENT ON COLUMN sale_items.subtotal is 'Subtotal del producto considerando cantidad, precio unitario y descuento aplicado.';


-- ==========================================
-- Tabla de pagos
-- ==========================================
CREATE TABLE payments (
    id BIGINT PRIMARY KEY DEFAULT nextval('payments_seq'),
    sale_id BIGINT NOT NULL,
    cash_session_id BIGINT NOT NULL,
    payment_method VARCHAR(30) NOT NULL,
    amount NUMERIC(12,2) NOT NULL,
    received_amount NUMERIC(12,2),
    change_amount NUMERIC(12,2),
    reference VARCHAR(100),
    created_by BIGINT,
    created_at TIMESTAMP DEFAULT NOW(),

    CONSTRAINT fk_payments_sale
        FOREIGN KEY (sale_id)
        REFERENCES sales(id),

    CONSTRAINT fk_payments_cash_session
        FOREIGN KEY (cash_session_id)
        REFERENCES cash_sessions(id)
);

-- ==========================================
-- Comentarios de la tabla payments
-- ==========================================

COMMENT ON TABLE payments is 'Tabla de pagos asociados a las ventas. Permite registrar uno o varios medios de pago para una misma venta.';

COMMENT ON COLUMN payments.id is 'Identificador único del pago.';
COMMENT ON COLUMN payments.sale_id is 'Identificador de la venta a la que pertenece el pago.';
COMMENT ON COLUMN payments.cash_session_id is 'Identificador de la sesión de caja en la que se registró el pago.';
COMMENT ON COLUMN payments.payment_method is 'Medio de pago utilizado. Ejemplos: CASH = efectivo, CARD = tarjeta, YAPE = Yape, PLIN = Plin, TRANSFER = transferencia.';
COMMENT ON COLUMN payments.amount is 'Importe de la venta cubierto mediante el medio de pago indicado.';
COMMENT ON COLUMN payments.received_amount is 'Monto entregado por el cliente. Solo se registra para pagos en efectivo.';
COMMENT ON COLUMN payments.change_amount is 'Vuelto entregado al cliente. Solo se registra para pagos en efectivo.';
COMMENT ON COLUMN payments.reference is 'Referencia de la operación de pago. Puede contener número de operación, código de transacción u otro identificador proporcionado por el medio de pago.';
COMMENT ON COLUMN payments.created_by is 'Usuario que registró el pago.';
COMMENT ON COLUMN payments.created_at is 'Fecha y hora en que se registró el pago.';



-- ==========================================
-- TABLA: JORNADA / SESIÓN DE CONTEO
-- Una sesión representa el control de inventario
-- realizado durante un día de trabajo.
-- ==========================================

CREATE TABLE inventory_count_sessions (
    id BIGINT PRIMARY KEY
        DEFAULT nextval('inventory_count_sessions_seq'),

    business_date DATE NOT NULL DEFAULT CURRENT_DATE,

    status VARCHAR(20) NOT NULL DEFAULT 'OPEN',
    -- OPEN: conteo iniciado
    -- REVIEW: existen diferencias pendientes de revisión
    -- CLOSED: conteo cerrado y conciliado
    -- CANCELLED: conteo cancelado

    opened_by BIGINT NOT NULL,
    opened_at TIMESTAMP NOT NULL DEFAULT NOW(),
    count_number VARCHAR(30) UNIQUE,

    closed_by BIGINT,
    closed_at TIMESTAMP,

    total_products BIGINT NOT NULL DEFAULT 0,
    counted_products BIGINT NOT NULL DEFAULT 0,
    matched_products BIGINT NOT NULL DEFAULT 0,
    shortage_products BIGINT NOT NULL DEFAULT 0,
    surplus_products BIGINT NOT NULL DEFAULT 0,

    opening_comment VARCHAR(500),
    closing_comment VARCHAR(500),

    created_by BIGINT,
    created_at TIMESTAMP DEFAULT NOW(),
    modified_by BIGINT,
    modified_at TIMESTAMP,
    deleted_by BIGINT,
    deleted_at TIMESTAMP,

    CONSTRAINT fk_inventory_count_sessions_opened_by
        FOREIGN KEY (opened_by)
        REFERENCES users(id),

    CONSTRAINT fk_inventory_count_sessions_closed_by
        FOREIGN KEY (closed_by)
        REFERENCES users(id),

    CONSTRAINT ck_inventory_count_sessions_status
        CHECK (status IN ('OPEN', 'REVIEW', 'CLOSED', 'CANCELLED'))
);

-- ==========================================
-- COMENTARIOS: TABLA inventory_count_sessions
-- ==========================================

COMMENT ON TABLE inventory_count_sessions is 'Jornada diaria de conteo físico de inventario. Permite comparar stock inicial, movimientos del día y stock físico final.';
COMMENT ON COLUMN inventory_count_sessions.id is 'Identificador único de la sesión de conteo.';
COMMENT ON COLUMN inventory_count_sessions.business_date is 'Fecha operativa a la que pertenece el conteo de inventario.';
COMMENT ON COLUMN inventory_count_sessions.status is 'Estado del conteo: OPEN, REVIEW, CLOSED o CANCELLED.';
COMMENT ON COLUMN inventory_count_sessions.opened_by is 'Usuario responsable de iniciar el conteo.';
COMMENT ON COLUMN inventory_count_sessions.opened_at is 'Fecha y hora de inicio de la jornada de conteo.';
COMMENT ON COLUMN inventory_count_sessions.count_number IS 'Número único de identificación de la sesión de conteo.';
COMMENT ON COLUMN inventory_count_sessions.closed_by is 'Usuario responsable de cerrar la jornada de conteo.';
COMMENT ON COLUMN inventory_count_sessions.closed_at is 'Fecha y hora de cierre de la jornada de conteo.';
COMMENT ON COLUMN inventory_count_sessions.total_products is 'Cantidad total de productos incluidos en la sesión de conteo.';
COMMENT ON COLUMN inventory_count_sessions.counted_products is 'Cantidad de productos que ya tienen conteo físico registrado.';
COMMENT ON COLUMN inventory_count_sessions.matched_products is 'Cantidad de productos cuyo stock físico coincide con el stock esperado.';
COMMENT ON COLUMN inventory_count_sessions.shortage_products is 'Cantidad de productos con faltante físico respecto al stock esperado.';
COMMENT ON COLUMN inventory_count_sessions.surplus_products is 'Cantidad de productos con sobrante físico respecto al stock esperado.';
COMMENT ON COLUMN inventory_count_sessions.opening_comment is 'Observación registrada al iniciar el conteo.';
COMMENT ON COLUMN inventory_count_sessions.closing_comment is 'Observación general registrada al cerrar el conteo.';


-- ==========================================
-- TABLA: DETALLE DE PRODUCTOS CONTADOS
-- Guarda el stock inicial, esperado y físico.
-- ==========================================

CREATE TABLE inventory_count_items (
    id BIGINT PRIMARY KEY
        DEFAULT nextval('inventory_count_items_seq'),

    session_id BIGINT NOT NULL,
    product_id BIGINT NOT NULL,

    -- Foto del stock existente cuando inicia la jornada.
    opening_stock BIGINT NOT NULL DEFAULT 0,

    -- Stock que calcula el sistema al terminar,
    -- usando ventas, ingresos, mermas y ajustes.
    expected_stock BIGINT,

    -- Stock ingresado mediante conteo físico.
    physical_stock BIGINT,

    -- physical_stock - expected_stock
    difference BIGINT,

    result_status VARCHAR(20) NOT NULL DEFAULT 'PENDING',
    -- PENDING: todavía no se contó
    -- MATCHED: físico y esperado coinciden
    -- SHORTAGE: falta producto
    -- SURPLUS: existe producto de más

    -- Motivo de la diferencia, obligatorio si se ajusta.
    difference_reason VARCHAR(255),
    difference_comment VARCHAR(500),

    -- Estado del ajuste de inventario.
    adjustment_status VARCHAR(20) NOT NULL DEFAULT 'NOT_REQUIRED',
    -- NOT_REQUIRED: no existe diferencia
    -- PENDING: diferencia pendiente de aprobación
    -- APPLIED: ajuste aplicado al inventario
    -- REJECTED: ajuste rechazado; se debe volver a contar

    adjustment_movement_id BIGINT,

    counted_by BIGINT,
    counted_at TIMESTAMP,

    approved_by BIGINT,
    approved_at TIMESTAMP,
    approval_comment VARCHAR(500),

    created_by BIGINT,
    created_at TIMESTAMP DEFAULT NOW(),
    modified_by BIGINT,
    modified_at TIMESTAMP,

    CONSTRAINT fk_inventory_count_items_session
        FOREIGN KEY (session_id)
        REFERENCES inventory_count_sessions(id)
        ON DELETE CASCADE,

    CONSTRAINT fk_inventory_count_items_product
        FOREIGN KEY (product_id)
        REFERENCES products(id),

    CONSTRAINT fk_inventory_count_items_counted_by
        FOREIGN KEY (counted_by)
        REFERENCES users(id),

    CONSTRAINT fk_inventory_count_items_approved_by
        FOREIGN KEY (approved_by)
        REFERENCES users(id),

    -- Movimiento ADJUSTMENT que se genera al aprobar la diferencia.
    CONSTRAINT fk_inventory_count_items_adjustment_movement
        FOREIGN KEY (adjustment_movement_id)
        REFERENCES inventory_movements(id),

    CONSTRAINT uq_inventory_count_item_product
        UNIQUE (session_id, product_id),

    CONSTRAINT ck_inventory_count_items_result_status
        CHECK (
            result_status IN (
                'PENDING',
                'MATCHED',
                'SHORTAGE',
                'SURPLUS'
            )
        ),

    CONSTRAINT ck_inventory_count_items_adjustment_status
        CHECK (
            adjustment_status IN (
                'NOT_REQUIRED',
                'PENDING',
                'APPLIED',
                'REJECTED'
            )
        )
);

-- ==========================================
-- COMENTARIOS: TABLA inventory_count_items
-- ==========================================

COMMENT ON TABLE inventory_count_items is 'Detalle por producto de una jornada de conteo. Guarda stock inicial, esperado, físico, diferencia y ajuste autorizado.';
COMMENT ON COLUMN inventory_count_items.session_id is 'Identificador de la jornada de conteo a la que pertenece el producto.';
COMMENT ON COLUMN inventory_count_items.product_id is 'Producto incluido en el conteo físico.';
COMMENT ON COLUMN inventory_count_items.opening_stock is 'Foto del stock del producto al momento de iniciar la jornada.';
COMMENT ON COLUMN inventory_count_items.expected_stock is 'Stock calculado automáticamente por el sistema al finalizar el conteo, antes de aplicar un posible ajuste.';
COMMENT ON COLUMN inventory_count_items.physical_stock is 'Cantidad física ingresada por el usuario durante el conteo.';
COMMENT ON COLUMN inventory_count_items.difference is 'Diferencia calculada como stock físico menos stock esperado. Negativo representa faltante; positivo representa sobrante.';
COMMENT ON COLUMN inventory_count_items.result_status is 'Resultado del conteo: PENDING, MATCHED, SHORTAGE o SURPLUS.';
COMMENT ON COLUMN inventory_count_items.difference_reason is 'Motivo seleccionado para justificar una diferencia de inventario.';
COMMENT ON COLUMN inventory_count_items.difference_comment is 'Detalle adicional de la diferencia detectada durante el conteo.';
COMMENT ON COLUMN inventory_count_items.adjustment_status is 'Estado del ajuste: NOT_REQUIRED, PENDING, APPLIED o REJECTED.';
COMMENT ON COLUMN inventory_count_items.adjustment_movement_id is 'Movimiento de inventario tipo ADJUSTMENT creado al aprobar una diferencia.';
COMMENT ON COLUMN inventory_count_items.counted_by is 'Usuario que registró el conteo físico del producto.';
COMMENT ON COLUMN inventory_count_items.counted_at is 'Fecha y hora en que se registró el conteo físico.';
COMMENT ON COLUMN inventory_count_items.approved_by is 'Usuario autorizado que aprobó o rechazó el ajuste de inventario.';
COMMENT ON COLUMN inventory_count_items.approved_at is 'Fecha y hora de aprobación o rechazo del ajuste.';
COMMENT ON COLUMN inventory_count_items.approval_comment is 'Comentario de aprobación, rechazo o justificación final del ajuste.';



-- ==========================================
-- Tabla de notificaciones
-- Una alerta se crea una vez y puede llegar a varios usuarios.
-- ==========================================
CREATE TABLE notifications (
    id BIGINT PRIMARY KEY DEFAULT nextval('notifications_seq'),

    -- Clave única del evento que originó la alerta.
    -- Evita duplicados si el mismo evento se procesa nuevamente.
    event_key VARCHAR(160) NOT NULL UNIQUE,

    -- Ejemplos: PAYMENT_RECEIVED, LOW_STOCK, WASTE_RECORDED,
    -- CASH_DIFFERENCE, INVENTORY_COUNT_DIFFERENCE.
    type VARCHAR(50) NOT NULL,

    -- Ejemplos: LOW, NORMAL, HIGH, CRITICAL.
    priority VARCHAR(20) NOT NULL,

    title VARCHAR(160) NOT NULL,
    message TEXT NOT NULL,

    -- Entidad relacionada con la alerta. Ejemplos: SALE, PRODUCT, CASH_SESSION.
    reference_type VARCHAR(50),
    reference_id BIGINT,

    -- Datos adicionales serializados en JSON para futuras alertas.
    metadata TEXT,

    created_at TIMESTAMP NOT NULL DEFAULT NOW()
);

-- ==========================================
-- Comentarios de notifications
-- ==========================================
COMMENT ON TABLE notifications IS 'Alertas internas del sistema. Su estructura genérica permite pagos digitales, stock bajo, mermas, diferencias de caja y conteos con diferencias.';
COMMENT ON COLUMN notifications.id IS 'Identificador único de la alerta.';
COMMENT ON COLUMN notifications.event_key IS 'Clave única del evento que originó la alerta. Se usa para impedir duplicados.';
COMMENT ON COLUMN notifications.type IS 'Tipo de alerta. Ejemplos: PAYMENT_RECEIVED, LOW_STOCK, WASTE_RECORDED, CASH_DIFFERENCE o INVENTORY_COUNT_DIFFERENCE.';
COMMENT ON COLUMN notifications.priority IS 'Nivel de prioridad de la alerta: LOW, NORMAL, HIGH o CRITICAL.';
COMMENT ON COLUMN notifications.title IS 'Título breve visible en el centro de notificaciones.';
COMMENT ON COLUMN notifications.message IS 'Detalle legible de la alerta.';
COMMENT ON COLUMN notifications.reference_type IS 'Tipo de entidad relacionada, por ejemplo SALE, PRODUCT, CASH_SESSION o INVENTORY_COUNT.';
COMMENT ON COLUMN notifications.reference_id IS 'Identificador de la entidad relacionada con la alerta.';
COMMENT ON COLUMN notifications.metadata IS 'Datos adicionales del evento serializados en JSON para uso interno y futuras notificaciones push.';
COMMENT ON COLUMN notifications.created_at IS 'Fecha y hora en que el sistema generó la alerta.';


-- ==========================================
-- Tabla intermedia usuario-notificaciones
-- Mantiene lectura y descarte de forma independiente por usuario.
-- ==========================================
CREATE TABLE user_notifications (
    id BIGINT PRIMARY KEY DEFAULT nextval('user_notifications_seq'),

    notification_id BIGINT NOT NULL,
    user_id BIGINT NOT NULL,

    read_at TIMESTAMP,
    dismissed_at TIMESTAMP,
    created_at TIMESTAMP NOT NULL DEFAULT NOW(),

    CONSTRAINT fk_user_notifications_notification
        FOREIGN KEY (notification_id)
        REFERENCES notifications(id)
        ON DELETE CASCADE,

    CONSTRAINT fk_user_notifications_user
        FOREIGN KEY (user_id)
        REFERENCES users(id)
        ON DELETE CASCADE,

    CONSTRAINT uk_user_notifications_notification_user
        UNIQUE (notification_id, user_id)
);

-- ==========================================
-- Comentarios de user_notifications
-- ==========================================
COMMENT ON TABLE user_notifications IS 'Asignación de alertas a usuarios. Cada destinatario conserva su propio estado de lectura o descarte.';
COMMENT ON COLUMN user_notifications.id IS 'Identificador único de la asignación de una alerta a un usuario.';
COMMENT ON COLUMN user_notifications.notification_id IS 'Alerta asignada al usuario.';
COMMENT ON COLUMN user_notifications.user_id IS 'Usuario destinatario de la alerta.';
COMMENT ON COLUMN user_notifications.read_at IS 'Fecha y hora en que el usuario leyó la alerta. Nulo si aún no la leyó.';
COMMENT ON COLUMN user_notifications.dismissed_at IS 'Fecha y hora en que el usuario descartó la alerta. Nulo si permanece visible.';
COMMENT ON COLUMN user_notifications.created_at IS 'Fecha y hora en que la alerta fue asignada al usuario.';



-- ==========================================
-- Tabla ventas canceladas
-- ==========================================
CREATE TABLE IF NOT EXISTS sale_cancellations (
    id BIGINT PRIMARY KEY DEFAULT nextval('sale_cancellations_seq'),
    sale_id BIGINT NOT NULL UNIQUE,
    reason VARCHAR(500) NOT NULL,
    cancelled_by BIGINT NOT NULL,
    cancelled_at TIMESTAMP NOT NULL DEFAULT NOW(),

    CONSTRAINT fk_sale_cancellations_sale
        FOREIGN KEY (sale_id) REFERENCES sales(id),
    CONSTRAINT fk_sale_cancellations_user
        FOREIGN KEY (cancelled_by) REFERENCES users(id)
);

-- ==========================================
-- Comentarios ventas canceladas
-- ==========================================
COMMENT ON TABLE sale_cancellations IS 'Auditoría de anulaciones totales de ventas. Conserva el motivo, responsable y fecha sin eliminar la venta ni sus pagos.';
COMMENT ON COLUMN sale_cancellations.sale_id IS 'Venta anulada. Solo puede existir un registro de anulación por venta.';
COMMENT ON COLUMN sale_cancellations.reason IS 'Motivo obligatorio proporcionado por el superadministrador.';
COMMENT ON COLUMN sale_cancellations.cancelled_by IS 'Usuario SUPER_ADMIN que realizó la anulación.';
COMMENT ON COLUMN sale_cancellations.cancelled_at IS 'Fecha y hora de la anulación.';

-- ==========================================
-- Índices
-- ==========================================
CREATE INDEX idx_users_email ON users(email);
CREATE INDEX idx_roles_name ON roles(name);
CREATE INDEX idx_permissions_name ON permissions(name);
CREATE INDEX idx_role_permissions_deleted_at ON role_permissions(deleted_at);


-- Sesiones de caja
CREATE INDEX idx_cash_sessions_register ON cash_sessions(cash_register_id);
CREATE INDEX idx_cash_sessions_opened_by ON cash_sessions(opened_by);
CREATE INDEX idx_cash_sessions_closed_by ON cash_sessions(closed_by);
CREATE INDEX idx_cash_sessions_status ON cash_sessions(status);
CREATE INDEX idx_cash_sessions_opened_at ON cash_sessions(opened_at);


-- Sesiones de inventario
CREATE INDEX idx_inventory_count_sessions_business_date ON inventory_count_sessions(business_date);
CREATE INDEX idx_inventory_count_sessions_status ON inventory_count_sessions(status);
CREATE INDEX idx_inventory_count_sessions_opened_by ON inventory_count_sessions(opened_by);
CREATE INDEX idx_inventory_count_items_session ON inventory_count_items(session_id);
CREATE INDEX idx_inventory_count_items_product ON inventory_count_items(product_id);
CREATE INDEX idx_inventory_count_items_result_status ON inventory_count_items(result_status);
CREATE INDEX idx_inventory_count_items_adjustment_status ON inventory_count_items(adjustment_status);

-- notificaciones
CREATE INDEX idx_notifications_created_at ON notifications(created_at DESC);
CREATE INDEX idx_notifications_type ON notifications(type);
CREATE INDEX idx_notifications_reference ON notifications(reference_type, reference_id);
CREATE INDEX idx_user_notifications_user_created_at ON user_notifications(user_id, created_at DESC);

CREATE INDEX IF NOT EXISTS idx_products_active_stock ON products(active, total_stock) WHERE deleted_at IS NULL;
CREATE INDEX IF NOT EXISTS idx_inventory_movements_product_created_at ON inventory_movements(product_id, created_at DESC) WHERE deleted_at IS NULL;
CREATE INDEX IF NOT EXISTS idx_cash_sessions_opened_alert ON cash_sessions(status, opened_at) WHERE deleted_at IS NULL;
CREATE INDEX IF NOT EXISTS idx_inventory_count_sessions_pending_alert ON inventory_count_sessions(status, opened_at);


-- Optimiza el contador y la lista de alertas pendientes de lectura.
CREATE INDEX idx_user_notifications_user_unread
ON user_notifications(user_id)
WHERE read_at IS NULL AND dismissed_at IS NULL;


-- ==========================================
-- Restricción:
-- Solo una sesión abierta por caja
-- ==========================================
CREATE UNIQUE INDEX uk_cash_sessions_open_register
ON cash_sessions(cash_register_id)
WHERE status = 'OPEN';


CREATE INDEX IF NOT EXISTS idx_sale_cancellations_cancelled_at
ON sale_cancellations(cancelled_at DESC);

-- Impide que existan dos conteos de inventario abiertos o en revisión.
-- Ejecutar una vez en bases existentes después de resolver cualquier duplicado previo.
CREATE UNIQUE INDEX IF NOT EXISTS uk_inventory_count_one_active_session
ON inventory_count_sessions ((1))
WHERE status IN ('OPEN', 'REVIEW');


CREATE INDEX IF NOT EXISTS idx_sale_cancellations_cancelled_at
ON sale_cancellations(cancelled_at DESC);

-- ==========================================
-- Inserts iniciales
-- ==========================================
-- Personas

INSERT INTO persons (type_document, document, full_name, phone, address)
VALUES (1, '12345678', 'Cristian Rodriguez', '999888777', 'Av. Principal 123');

-- Usuarios
INSERT INTO users (person_id, username, password, email, active)
VALUES (1, 'crodriguezz', '$2a$12$1kDlNLmmYb5c57fFsuHiyOgOoX.FvnkZR0TSFFjQtrLniswSGDaYi', 'cristian@empresa.com', TRUE);

-- Roles
INSERT INTO roles (name, description, created_by) VALUES ('SUPER_ADMIN', 'Rol con todos los privilegios del sistema', 1);

-- Asignar rol al usuario
INSERT INTO user_roles (user_id, role_id, assigned_by)
SELECT u.id, r.id, 1
FROM users u, roles r
WHERE u.username = 'crodriguezz' AND r.name = 'SUPER_ADMIN';

-- Permisos
INSERT INTO permissions (name, description, created_by, module) VALUES
-- Dashboard
('VIEW_DASHBOARD', 'Permiso para ver el dashboard', 1, 'Inicio'),

-- Usuarios
('VIEW_USER', 'Permiso para ver usuarios', 1, 'Mantenedor Usuario'),
('CREATE_USER', 'Permiso para crear usuarios', 1, 'Mantenedor Usuario'),
('EDIT_USER', 'Permiso para editar usuarios', 1, 'Mantenedor Usuario'),
('DELETE_USER', 'Permiso para eliminar usuarios', 1, 'Mantenedor Usuario'),
('EXPORT_USER', 'Permiso para exportar usuarios', 1, 'Mantenedor Usuario'),

-- Roles
('VIEW_ROLE', 'Permiso para ver roles', 1, 'Mantenedor Rol y Permiso'),
('CREATE_ROLE', 'Permiso para crear roles', 1, 'Mantenedor Rol y Permiso'),
('EDIT_ROLE', 'Permiso para editar roles', 1, 'Mantenedor Rol y Permiso'),
('DELETE_ROLE', 'Permiso para eliminar roles', 1, 'Mantenedor Rol y Permiso'),
('EXPORT_ROLE', 'Permiso para exportar roles', 1, 'Mantenedor Rol y Permiso'),

-- Permisos
('VIEW_PERMISSION', 'Permiso para ver permisos', 1, 'Mantenedor Rol y Permiso'),
('ASSIGN_PERMISSION', 'Permiso para asignar permisos a roles', 1, 'Mantenedor Rol y Permiso'),

-- Mi Perfil
('VIEW_PROFILE', 'Permiso para ver perfil', 1, 'Perfil de Usuario'),
('UPDATE_PROFILE', 'Permiso para actualizar perfil', 1, 'Perfil de Usuario'),
('PERMISSION_PROFILE', 'Permiso para ver mis permisos', 1, 'Perfil de Usuario'),

-- Roles
('VIEW_PARAMETER', 'Permiso para ver parámetros', 1, 'Mantenedor Parámetro'),
('CREATE_PARAMETER', 'Permiso para crear parámetros', 1, 'Mantenedor Parámetro'),
('EDIT_PARAMETER', 'Permiso para editar parámetros', 1, 'Mantenedor Parámetro'),
('DELETE_PARAMETER', 'Permiso para eliminar parámetros', 1, 'Mantenedor Parámetro'),
('EXPORT_PARAMETER', 'Permiso para exportar parámetros', 1, 'Mantenedor Parámetro'),

-- Productos
('VIEW_PRODUCT', 'Permiso para ver productos', 1, 'Mantenedor Producto'),
('CREATE_PRODUCT', 'Permiso para crear productos', 1, 'Mantenedor Producto'),
('EDIT_PRODUCT', 'Permiso para editar productos', 1, 'Mantenedor Producto'),
('DELETE_PRODUCT', 'Permiso para eliminar productos', 1, 'Mantenedor Producto'),
('EXPORT_PRODUCT', 'Permiso para exportar productos', 1, 'Mantenedor Producto');


-- Asignar permisos al rol
INSERT INTO role_permissions (role_id, permission_id, created_by)
SELECT r.id, p.id, 1
FROM roles r, permissions p
WHERE r.name = 'SUPER_ADMIN';

-- Parámetros
INSERT INTO parameters (parent_parameter_id, parameter_id, code, type, name, short_name, order_number, active, created_by, created_at)
values
-- TIPO DE PARÁMETRO
(NULL, 1, 'TIPO_PARAMETRO', 2, 'Archivo', 'ARCH', 1, TRUE, 1, now()),
(NULL, 2, 'TIPO_PARAMETRO', 2, 'Texto', 'TXT', 2, TRUE, 1, now()),
-- TIPO DE DOCUMENTO
(NULL, 1, 'TIPO_DOCUMENTO', 2, 'DNI', 'DNI', 1, TRUE, 1, now()),
(NULL, 2, 'TIPO_DOCUMENTO', 2, 'Carné de Extranjería', 'CE', 2, TRUE, 1, now()),
(NULL, 3, 'TIPO_DOCUMENTO', 2, 'Pasaporte', 'Pasaporte', 3, TRUE, 1, now()),
-- UNIDAD DE MEDIDA PRODUCTO
(NULL, 1, 'UNIDAD_MEDIDA_PRODUCTO', 2, 'Unidad', 'Und', 1, TRUE, 1, now()),
(NULL, 2, 'UNIDAD_MEDIDA_PRODUCTO', 2, 'Kilogramos', 'Kg', 2, TRUE, 1, now()),
-- CATEGORIA DE PRODUCTO
(NULL, 1, 'CATEGORIA_PRODUCTO', 2, 'Bebidas calientes', 'Calientes', 1, TRUE, 1, now()),
(NULL, 2, 'CATEGORIA_PRODUCTO', 2, 'Bebidas frías', 'Frías', 2, TRUE, 1, now()),
(NULL, 3, 'CATEGORIA_PRODUCTO', 2, 'Café en grano y molido', 'Café', 3, TRUE, 1, now()),
(NULL, 4, 'CATEGORIA_PRODUCTO', 2, 'Tés e infusiones', 'Tés', 4, TRUE, 1, now()),
(NULL, 5, 'CATEGORIA_PRODUCTO', 2, 'Pasteles, tortas y postres', 'Pastelería', 5, TRUE, 1, now()),
(NULL, 6, 'CATEGORIA_PRODUCTO', 2, 'Panes y bollería', 'Panadería', 6, TRUE, 1, now()),
(NULL, 7, 'CATEGORIA_PRODUCTO', 2, 'Sándwiches y emparedados', 'Sándwiches', 7, TRUE, 1, now()),
(NULL, 8, 'CATEGORIA_PRODUCTO', 2, 'Galletas, chips y bocadillos', 'Snacks', 8, TRUE, 1, now()),
(NULL, 9, 'CATEGORIA_PRODUCTO', 2, 'Jugos naturales', 'Jugos', 9, TRUE, 1, now()),
(NULL, 10, 'CATEGORIA_PRODUCTO', 2, 'Azúcar, jarabes y complementos', 'Complementos', 10, TRUE, 1, now()),
-- MÉTODO DE VALUACIÓN DE INVENTARIO
(NULL, 1, 'METODO_VALUACION', 2, 'FIFO (Primero en entrar, primero en salir)', 'FIFO', 1, TRUE, 1, now()),
(NULL, 2, 'METODO_VALUACION', 2, 'LIFO (Último en entrar, primero en salir)', 'LIFO', 2, TRUE, 1, now()),
(NULL, 3, 'METODO_VALUACION', 2, 'FEFO (Primero en vencer, primero en salir)', 'FEFO', 3, TRUE, 1, now());


-- ==========================================
-- Caja inicial
-- ==========================================

INSERT INTO cash_registers (code, name, description, active, created_by, created_at)
VALUES
('CAJA-01', 'Caja Principal', 'Caja principal para operaciones de venta.', TRUE, 1, NOW());


-- ==========================================
-- Reiniciar secuencias según los valores insertados
-- ==========================================
SELECT setval('persons_seq', (SELECT COALESCE(MAX(id),0) FROM persons));
SELECT setval('users_seq', (SELECT COALESCE(MAX(id),0) FROM users));
SELECT setval('roles_seq', (SELECT COALESCE(MAX(id),0) FROM roles));
SELECT setval('permissions_seq', (SELECT COALESCE(MAX(id),0) FROM permissions));
SELECT setval('parameters_seq', (SELECT COALESCE(MAX(id),0) FROM parameters));
SELECT setval('user_roles_seq', (SELECT COALESCE(MAX(id),0) FROM user_roles));
SELECT setval('cash_registers_seq', (SELECT COALESCE(MAX(id),0) FROM cash_registers));
/*SELECT setval('products_seq', (SELECT COALESCE(MAX(id),0) FROM products));
SELECT setval('product_images_seq', (SELECT COALESCE(MAX(id),0) FROM product_images));
SELECT setval('catalog_configs_seq', (SELECT COALESCE(MAX(id),0) FROM catalog_configs));
SELECT setval('inventory_movements_seq', (SELECT COALESCE(MAX(id),0) FROM inventory_movements));
SELECT setval('cash_sessions_seq', (SELECT COALESCE(MAX(id),0) FROM cash_sessions));
SELECT setval('orders_seq', (SELECT COALESCE(MAX(id),0) FROM orders));
SELECT setval('order_items_seq', (SELECT COALESCE(MAX(id),0) FROM order_items));
SELECT setval('sales_seq', (SELECT COALESCE(MAX(id),0) FROM sales));
SELECT setval('sale_items_seq', (SELECT COALESCE(MAX(id),0) FROM sale_items));
SELECT setval('payments_seq', (SELECT COALESCE(MAX(id),0) FROM payments));
SELECT setval('inventory_count_sessions_seq', (SELECT COALESCE(MAX(id),0) FROM inventory_count_sessions));
SELECT setval('inventory_count_items_seq', (SELECT COALESCE(MAX(id),0) FROM inventory_count_items));
SELECT setval('notifications_seq', COALESCE((SELECT MAX(id) FROM notifications), 1), EXISTS(SELECT 1 FROM notifications));
SELECT setval('user_notifications_seq', COALESCE((SELECT MAX(id) FROM user_notifications), 1), EXISTS(SELECT 1 FROM user_notifications));
SELECT setval('sale_cancellations_seq', COALESCE((SELECT MAX(id) FROM sale_cancellations), 1), EXISTS(SELECT 1 FROM sale_cancellations));


select * from products;*/
