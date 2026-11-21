CREATE OR REPLACE FUNCTION transactions_set_updated_at() RETURNS trigger AS $$
    BEGIN
        NEW.updated_at := now();
        RETURN NEW;
    END
$$ LANGUAGE plpgsql;


CREATE OR REPLACE TRIGGER transactions_set_updated_at
    BEFORE UPDATE ON transactions
    FOR EACH ROW
    EXECUTE FUNCTION transactions_set_updated_at();



CREATE OR REPLACE FUNCTION transactions_log_insert() RETURNS trigger AS $$
    BEGIN
        INSERT INTO transaction_logs (id, timestamp, transaction_id, from_status, to_status, changed_by, reason)
        VALUES (gen_random_uuid(),
                (NEW).created_at,
                (NEW).id,
                NULL,
                (NEW).status,
                NULLIF(current_setting('app.user_id', true), '')::uuid,
                (NEW).failure_reason);
        RETURN NULL;
    END
$$ LANGUAGE plpgsql;

CREATE OR REPLACE TRIGGER transactions_log_insert
    AFTER INSERT ON transactions
    FOR EACH ROW
    EXECUTE FUNCTION transactions_log_insert();



CREATE OR REPLACE FUNCTION transactions_log_update() RETURNS trigger AS $$
    BEGIN
        INSERT INTO transaction_logs (id, timestamp, transaction_id, from_status, to_status, changed_by, reason)
        VALUES (gen_random_uuid(),
                (NEW).updated_at,
                (NEW).id,
                (OLD).status,
                (NEW).status,
                NULLIF(current_setting('app.user_id', true), '')::uuid,
                (NEW).failure_reason);
        RETURN NULL;
    END
$$ LANGUAGE plpgsql;

CREATE OR REPLACE TRIGGER transactions_log_update
    AFTER UPDATE ON transactions
    FOR EACH ROW
    EXECUTE FUNCTION transactions_log_update();