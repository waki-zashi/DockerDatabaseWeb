CREATE TABLE IF NOT EXISTS contacts (
    id            SERIAL          PRIMARY KEY,
    full_name     VARCHAR(100)    NOT NULL,
    phone_number  VARCHAR(20)     NOT NULL,
    note          VARCHAR(500),
    created_at    TIMESTAMP       DEFAULT CURRENT_TIMESTAMP,
    updated_at    TIMESTAMP       DEFAULT CURRENT_TIMESTAMP
);

CREATE INDEX IF NOT EXISTS idx_phone_number ON contacts(phone_number);
CREATE INDEX IF NOT EXISTS idx_full_name    ON contacts(full_name);

CREATE OR REPLACE FUNCTION validate_contact()
RETURNS TRIGGER AS $$
BEGIN
    IF NEW.full_name IS NULL OR trim(NEW.full_name) = '' THEN
        RAISE EXCEPTION 'Full name cannot be empty or consist only of spaces';
    END IF;

    IF length(trim(NEW.full_name)) < 2 THEN
        RAISE EXCEPTION 'Full name must contain at least 2 characters (ignoring leading/trailing spaces)';
    END IF;

    IF length(NEW.full_name) > 100 THEN
        RAISE EXCEPTION 'Full name cannot be longer than 100 characters';
    END IF;

    IF NEW.full_name !~ '^[A-Za-zА-Яа-яЁё\s-]+$' THEN
        RAISE EXCEPTION 'Full name can only contain letters (Latin and Cyrillic), spaces and hyphens';
    END IF;

    IF NEW.phone_number IS NULL OR trim(NEW.phone_number) = '' THEN
        RAISE EXCEPTION 'Phone number is required';
    END IF;

    IF length(NEW.phone_number) < 5 OR length(NEW.phone_number) > 20 THEN
        RAISE EXCEPTION 'Phone number must contain between 5 and 20 characters';
    END IF;

    IF NEW.phone_number !~ '^[0-9+() -]+$' THEN
        RAISE EXCEPTION 'Phone number can only contain digits, +, (, ), space and hyphen';
    END IF;

    IF NEW.note IS NOT NULL AND length(NEW.note) > 500 THEN
        RAISE EXCEPTION 'Note cannot exceed 500 characters';
    END IF;

    RETURN NEW;
END;
$$ LANGUAGE plpgsql;


DROP TRIGGER IF EXISTS trg_validate_contact ON contacts;
CREATE TRIGGER trg_validate_contact
    BEFORE INSERT OR UPDATE
    ON contacts
    FOR EACH ROW
    EXECUTE FUNCTION validate_contact();

CREATE OR REPLACE FUNCTION update_updated_at_column()
RETURNS TRIGGER AS $$
BEGIN
    NEW.updated_at = CURRENT_TIMESTAMP;
    RETURN NEW;
END;
$$ LANGUAGE plpgsql;

DROP TRIGGER IF EXISTS trg_update_updated_at ON contacts;
CREATE TRIGGER trg_update_updated_at
    BEFORE UPDATE
    ON contacts
    FOR EACH ROW
    EXECUTE FUNCTION update_updated_at_column();

COMMENT ON TABLE contacts IS 'Phone book contacts with strict validation rules';
COMMENT ON COLUMN contacts.full_name    IS 'Full name (2–100 chars, letters, spaces, hyphens only)';
COMMENT ON COLUMN contacts.phone_number  IS 'Phone number (5–20 chars, digits + () - + space)';
COMMENT ON COLUMN contacts.note          IS 'Optional note, max 500 characters';