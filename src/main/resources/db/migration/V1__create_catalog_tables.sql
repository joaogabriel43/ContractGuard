-- Create services table
CREATE TABLE services (
    id UUID PRIMARY KEY,
    name VARCHAR(255) NOT NULL,
    slug VARCHAR(255) NOT NULL UNIQUE,
    created_at TIMESTAMP WITHOUT TIME ZONE NOT NULL
);

-- Create api_specifications table
CREATE TABLE api_specifications (
    id UUID PRIMARY KEY,
    service_id UUID NOT NULL,
    version VARCHAR(50) NOT NULL,
    raw_content JSONB NOT NULL,
    created_at TIMESTAMP WITHOUT TIME ZONE NOT NULL,
    CONSTRAINT fk_service FOREIGN KEY (service_id) REFERENCES services(id) ON DELETE CASCADE,
    CONSTRAINT uq_service_version UNIQUE (service_id, version)
);

-- Index on JSONB for potential future querying
CREATE INDEX idx_api_specs_raw_content ON api_specifications USING GIN (raw_content);
