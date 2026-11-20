CREATE OR REPLACE FUNCTION set_transaction_updated_at()
RETURNS trigger
LANGUAGE plpgsql
AS $$
BEGIN
    NEW.updated_at = clock_timestamp();
    RETURN NEW;
END;
$$;

CREATE TRIGGER transactions_set_updated_at
BEFORE UPDATE ON transactions
FOR EACH ROW
EXECUTE FUNCTION set_transaction_updated_at();

CREATE OR REPLACE FUNCTION log_transaction_change()
RETURNS trigger
LANGUAGE plpgsql
AS $$
BEGIN
    IF TG_OP = 'INSERT' THEN
        INSERT INTO transaction_logs (
            id,
            created_at,
            transaction_id,
            from_status,
            to_status,
            changed_by,
            reason
        ) VALUES (
            uuidv7(),
            NEW.created_at,
            NEW.id,
            NULL,
            NEW.status,
            NULL,
            NEW.failure_reason
        );
    ELSE
        INSERT INTO transaction_logs (
            id,
            created_at,
            transaction_id,
            from_status,
            to_status,
            changed_by,
            reason
        ) VALUES (
            uuidv7(),
            NEW.updated_at,
            NEW.id,
            OLD.status,
            NEW.status,
            NULL,
            NEW.failure_reason
        );
    END IF;

    RETURN NEW;
END;
$$;

CREATE TRIGGER transactions_log_insert
AFTER INSERT ON transactions
    FOR EACH ROW
EXECUTE FUNCTION log_transaction_change();

CREATE TRIGGER transactions_log_update
AFTER UPDATE ON transactions
    FOR EACH ROW
EXECUTE FUNCTION log_transaction_change();