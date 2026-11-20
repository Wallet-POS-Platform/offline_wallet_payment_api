CREATE TABLE "transaction_logs" (
    id             uuid        PRIMARY KEY,
    timestamp      timestamptz NOT NULL DEFAULT now(),
    transaction_id uuid        NOT NULL REFERENCES transactions (id),
    from_status    text,
    to_status      text        NOT NULL,
    changed_by     uuid        REFERENCES users (id),
    reason         text
);