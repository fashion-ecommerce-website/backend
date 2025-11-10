-- Add new columns to product_metadata table for enhanced product analysis
ALTER TABLE product_metadata
    ADD COLUMN IF NOT EXISTS colors JSONB DEFAULT '[]'::jsonb,
    ADD COLUMN IF NOT EXISTS patterns JSONB DEFAULT '[]'::jsonb,
    ADD COLUMN IF NOT EXISTS materials JSONB DEFAULT '[]'::jsonb,
    ADD COLUMN IF NOT EXISTS seasons JSONB DEFAULT '[]'::jsonb,
    ADD COLUMN IF NOT EXISTS formality_level INTEGER DEFAULT 3,
    ADD COLUMN IF NOT EXISTS warmth_level INTEGER DEFAULT 3,
    ADD COLUMN IF NOT EXISTS ai_analysis TEXT,
    ADD COLUMN IF NOT EXISTS last_analyzed_at TIMESTAMP,
    ADD COLUMN IF NOT EXISTS version BIGINT DEFAULT 0;

-- Update existing records to have default values
UPDATE product_metadata 
SET 
    colors = COALESCE(colors, '[]'::jsonb),
    patterns = COALESCE(patterns, '[]'::jsonb),
    materials = COALESCE(materials, '[]'::jsonb),
    seasons = COALESCE(seasons, '[]'::jsonb),
    formality_level = COALESCE(formality_level, 3),
    warmth_level = COALESCE(warmth_level, 3),
    version = COALESCE(version, 0),
    ai_analysis = gemini_analysis;
