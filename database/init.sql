-- Create addresses table
CREATE TABLE IF NOT EXISTS addresses (
    id BIGSERIAL PRIMARY KEY,
    user_id BIGINT NOT NULL,
    full_name TEXT NOT NULL,
    phone TEXT NOT NULL,
    line TEXT NOT NULL,
    ward TEXT,
    city TEXT NOT NULL,
    province TEXT,
    country_code TEXT DEFAULT 'VN',
    postal_code TEXT,
    is_default BOOLEAN DEFAULT FALSE,
    created_at TIMESTAMP DEFAULT NOW(),
    updated_at TIMESTAMP DEFAULT NOW(),
    CONSTRAINT fk_address_user FOREIGN KEY (user_id) REFERENCES users(id) ON DELETE CASCADE
);

-- Create indexes for addresses table
CREATE INDEX IF NOT EXISTS idx_address_user_id ON addresses(user_id);
CREATE INDEX IF NOT EXISTS idx_address_default ON addresses(user_id, is_default);

-- Create trigger to update updated_at column
CREATE OR REPLACE FUNCTION update_updated_at_column()
RETURNS TRIGGER AS $$
BEGIN
    NEW.updated_at = NOW();
    RETURN NEW;
END;
$$ language 'plpgsql';

CREATE TRIGGER update_addresses_updated_at 
    BEFORE UPDATE ON addresses 
    FOR EACH ROW 
    EXECUTE FUNCTION update_updated_at_column(); 