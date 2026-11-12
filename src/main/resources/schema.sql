CREATE TABLE IF NOT EXISTS api_idempotency (
    idempotency_key VARCHAR(260) PRIMARY KEY,
    resource_name VARCHAR(50) NOT NULL,
    resource_id UUID NOT NULL
);

CREATE TABLE IF NOT EXISTS api_keys (
    id UUID PRIMARY KEY,
    key_hash VARCHAR(64) NOT NULL UNIQUE,
    usuario_id UUID NOT NULL,
    created_at TIMESTAMP WITH TIME ZONE NOT NULL,
    revoked_at TIMESTAMP WITH TIME ZONE
);
