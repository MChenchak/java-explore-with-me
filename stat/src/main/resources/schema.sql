CREATE TABLE IF NOT EXISTS hit
(
    id    serial NOT NULL PRIMARY KEY,
    app  VARCHAR(255)          NOT NULL,
    uri VARCHAR(255)           NOT NULL,
    ip VARCHAR(255)            NOT NULL,
    hitted timestamp
);