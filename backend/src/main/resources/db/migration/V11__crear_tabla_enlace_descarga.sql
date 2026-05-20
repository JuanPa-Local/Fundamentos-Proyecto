CREATE TABLE enlace_descarga (
    id          UUID PRIMARY KEY DEFAULT gen_random_uuid(),
    buyer_id    UUID NOT NULL,
    libro_id    UUID NOT NULL,
    token       VARCHAR(255) NOT NULL UNIQUE,
    expiracion  TIMESTAMP NOT NULL,
    usado       BOOLEAN NOT NULL DEFAULT FALSE,
    created_at  TIMESTAMP NOT NULL DEFAULT now()
);

CREATE TABLE registro_descarga (
    id          UUID PRIMARY KEY DEFAULT gen_random_uuid(),
    buyer_id    UUID NOT NULL,
    libro_id    UUID NOT NULL,
    ip_origen   VARCHAR(45),
    timestamp   TIMESTAMP NOT NULL DEFAULT now()
);

CREATE INDEX idx_enlace_token ON enlace_descarga (token);
