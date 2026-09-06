CREATE TYPE payment_status AS ENUM ('PENDING', 'APPROVED', 'REJECTED');

CREATE TABLE payment (
    payment_id BIGINT GENERATED ALWAYS AS IDENTITY PRIMARY KEY,
    order_id BIGINT NOT NULL,
    amount NUMERIC(10,2) NOT NULL CHECK (amount >= 0),
    status payment_status NOT NULL DEFAULT 'PENDING',
    created_at TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP,
    updated_at TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP
);

CREATE TABLE processed_event (
    event_id UUID PRIMARY KEY,
    order_id BIGINT NOT NULL,
    processed_at TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP
);

CREATE INDEX idx_payment_order_id ON payment(order_id);
CREATE INDEX idx_processedevent_order_id ON processed_event(order_id);
