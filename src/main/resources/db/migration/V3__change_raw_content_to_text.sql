-- Drop GIN index (requires JSONB, incompatible with TEXT)
DROP INDEX IF EXISTS idx_api_specs_raw_content;

-- Change raw_content from JSONB to TEXT
-- TEXT accepts any string (YAML or JSON) without type validation,
-- keeping all spec content faithful to what was submitted.
ALTER TABLE api_specifications
    ALTER COLUMN raw_content TYPE TEXT;
