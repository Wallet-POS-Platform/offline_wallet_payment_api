CREATE TABLE "users" (
    id            uuid        PRIMARY KEY,
    created_at    timestamptz NOT NULL DEFAULT now(),
    type          text        NOT NULL,
    username      text        NOT NULL UNIQUE,
    password_hash text        NOT NULL,
    is_active     boolean     NOT NULL DEFAULT TRUE,

    CONSTRAINT chk_user_type CHECK (type IN ('CLIENT', 'MERCHANT'))
);

CREATE TABLE "clients" (
    id                   uuid  PRIMARY KEY REFERENCES users (id),
    first_name           text NOT NULL,
    last_name            text NOT NULL,
    address              text,
    phone_number         text,
    country              text NOT NULL,
    public_key           bytea NOT NULL UNIQUE,
    is_public_key_active boolean NOT NULL DEFAULT TRUE
);

CREATE TABLE "merchants" (
    id          uuid PRIMARY KEY REFERENCES users (id),
    name        text NOT NULL,
    category    text,
    address     text,
    payout_iban text,
    settle_type text NOT NULL
);

CREATE TABLE "terminals" (
    id             uuid        PRIMARY KEY,
    created_at     timestamptz NOT NULL DEFAULT now(),
    updated_at     timestamptz NOT NULL DEFAULT now(),
    merchant_id    uuid        NOT NULL REFERENCES merchants (id),
    name           text        NOT NULL,
    cert_id        text        NOT NULL UNIQUE,
    is_cert_active boolean NOT NULL DEFAULT TRUE,
    is_active      boolean     NOT NULL DEFAULT TRUE
);

CREATE TABLE "wallets" (
    id          uuid          PRIMARY KEY,
    created_at  timestamptz   NOT NULL DEFAULT now(),
    user_id     uuid          REFERENCES users (id),
    wallet_type text          NOT NULL,
    name        text          NOT NULL,
    currency    text          NOT NULL DEFAULT 'EUR',
    balance     numeric(10,2) NOT NULL DEFAULT 0,
    is_active   boolean       NOT NULL DEFAULT TRUE
);

CREATE TABLE "transactions" (
    id             uuid          PRIMARY KEY,
    created_at     timestamptz   NOT NULL DEFAULT now(),
    updated_at     timestamptz   NOT NULL DEFAULT now(),
    type           text          NOT NULL,
    status         text          NOT NULL,
    from_wallet_id uuid          REFERENCES wallets (id),
    to_wallet_id   uuid          REFERENCES wallets (id),
    amount         numeric(10,2) NOT NULL,
    vat_rate       numeric(4,2)  NOT NULL DEFAULT 0,
    currency       text          NOT NULL DEFAULT 'EUR',
    failure_reason text,

    CONSTRAINT chk_transaction_status CHECK (status IN ('PENDING', 'SUCCESS', 'CANCELED'))
);

CREATE TABLE "ledger" (
    id          uuid          PRIMARY KEY,
    created_at  timestamptz   NOT NULL DEFAULT now(),
    trans_id    uuid          NOT NULL REFERENCES transactions (id),
    ref_id      uuid          REFERENCES transactions (id),
    wallet_id   uuid          NOT NULL REFERENCES wallets (id),
    amount      numeric(10,2) NOT NULL,
    vat         numeric(10,2) NOT NULL DEFAULT 0,
    vat_rate    numeric(4,2)  NOT NULL DEFAULT 0,
    currency    text          NOT NULL DEFAULT 'EUR',
    type        text          NOT NULL
);