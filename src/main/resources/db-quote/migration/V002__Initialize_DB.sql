CREATE TABLE quote
(
    id        BIGSERIAL    PRIMARY KEY,
    text      VARCHAR(512) NOT NULL,
    source    VARCHAR(144) NOT NULL,
    tag       VARCHAR(32)  NULL,
    link      VARCHAR(128) NULL
);
