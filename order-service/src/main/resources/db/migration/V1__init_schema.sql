CREATE TYPE order_status AS ENUM (
    'CREATED', 'PAYMENT_PENDING', 'PAYMENT_APPROVED',
    'INVENTORY_PENDING', 'CONFIRMED', 'CANCELLED'
);

CREATE TYPE order_event_type AS ENUM (
    'ORDER_CREATED', 'PAYMENT_REQUESTED', 'PAYMENT_APPROVED', 'PAYMENT_REJECTED',
    'INVENTORY_RESERVED', 'INVENTORY_UNAVAILABLE', 'ORDER_CONFIRMED', 'ORDER_CANCELLED'
);

CREATE TABLE "order" (
     order_id BIGINT GENERATED ALWAYS AS IDENTITY PRIMARY KEY,
     status order_status NOT NULL DEFAULT 'CREATED',
     cancel_reason VARCHAR(100),
     total_amount NUMERIC(10,2) NOT NULL CHECK (total_amount >= 0),
     created_at TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP,
     updated_at TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP
);

CREATE TABLE order_item (
    order_item_id BIGINT GENERATED ALWAYS AS IDENTITY PRIMARY KEY,
    order_id BIGINT NOT NULL,
    product_id BIGINT NOT NULL,
    product_name VARCHAR(50) NOT NULL,
    quantity INT NOT NULL CHECK (quantity > 0),
    unit_price NUMERIC(10,2) NOT NULL CHECK (unit_price >= 0),
    CONSTRAINT fk_orderitem_order FOREIGN KEY (order_id) REFERENCES "order"(order_id)
);

CREATE TABLE order_event (
     order_event_id BIGINT GENERATED ALWAYS AS IDENTITY PRIMARY KEY,
     order_id BIGINT NOT NULL,
     event_id UUID NOT NULL UNIQUE,
     event_type order_event_type NOT NULL,
     payload JSONB NOT NULL,
     created_at TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP,
     CONSTRAINT fk_orderevent_order FOREIGN KEY (order_id) REFERENCES "order"(order_id)
);

CREATE INDEX idx_orderitem_order_id ON order_item(order_id);
CREATE INDEX idx_orderitem_product_id ON order_item(product_id);
CREATE INDEX idx_orderevent_order_id ON order_event(order_id);
CREATE INDEX idx_orderevent_type ON order_event(event_type);