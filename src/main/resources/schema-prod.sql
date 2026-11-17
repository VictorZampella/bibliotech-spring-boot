CREATE TABLE IF NOT EXISTS api_idempotency (
    idempotency_key VARCHAR(260) PRIMARY KEY,
    resource_name VARCHAR(50) NOT NULL,
    resource_id UUID NOT NULL
);@@

CREATE TABLE IF NOT EXISTS api_keys (
    id UUID PRIMARY KEY,
    key_hash VARCHAR(64) NOT NULL UNIQUE,
    usuario_id UUID NOT NULL,
    created_at TIMESTAMP WITH TIME ZONE NOT NULL,
    revoked_at TIMESTAMP WITH TIME ZONE
);@@

ALTER TABLE usuario ADD COLUMN IF NOT EXISTS papel VARCHAR(20) NOT NULL DEFAULT 'MEMBRO';@@

ALTER TABLE usuario ADD COLUMN IF NOT EXISTS plano VARCHAR(20) NOT NULL DEFAULT 'BASE';@@

ALTER TABLE livro ADD COLUMN IF NOT EXISTS quantidade_exemplares INTEGER NOT NULL DEFAULT 0;@@

DO $$
BEGIN
    IF NOT EXISTS (
        SELECT 1
        FROM pg_constraint
        WHERE conname = 'ck_livro_quantidade_exemplares'
          AND conrelid = 'livro'::regclass
    ) THEN
        ALTER TABLE livro
            ADD CONSTRAINT ck_livro_quantidade_exemplares
            CHECK (quantidade_exemplares >= 0);
    END IF;
END
$$;@@
