
------------------------------------------------------------------------------------------------------------------------
-- Schema sui_bi -------------------------------------------------------------------------------------------------------
------------------------------------------------------------------------------------------------------------------------

CREATE SCHEMA sui_bi;

------------------------------------------------------------------------------------------------------------------------
-- Table sui_bi.users --------------------------------------------------------------------------------------------------
------------------------------------------------------------------------------------------------------------------------

CREATE TABLE sui_bi.users
(
    id               BIGSERIAL   NOT NULL PRIMARY KEY,
    -- audit
    created          TIMESTAMPTZ NOT NULL DEFAULT now(),
    creator_id       BIGINT REFERENCES sui_bi.users (id),
    last_modified    TIMESTAMPTZ NOT NULL DEFAULT now(),
    last_modifier_id BIGINT REFERENCES sui_bi.users (id),
    -- payload
    name             TEXT        NOT NULL,
    username         TEXT        NOT NULL,
    email            TEXT        NOT NULL,
    password         TEXT        NOT NULL,
    deleted          BOOLEAN     NOT NULL DEFAULT FALSE
);

CREATE INDEX users_username_index ON sui_bi.users (username);
CREATE INDEX users_email_index ON sui_bi.users (email);
CREATE UNIQUE INDEX users_case_insensitive_username_uindex ON sui_bi.users (LOWER(username));
CREATE UNIQUE INDEX users_case_insensitive_email_uindex ON sui_bi.users (LOWER(email));

COMMENT ON TABLE sui_bi.users IS 'Пользователи';
COMMENT ON COLUMN sui_bi.users.id IS 'ИД записи (PK)';
COMMENT ON COLUMN sui_bi.users.created IS 'Дата и время создания';
COMMENT ON COLUMN sui_bi.users.creator_id IS 'ИД создателя';
COMMENT ON COLUMN sui_bi.users.last_modified IS 'Дата и время последнего изменения';
COMMENT ON COLUMN sui_bi.users.last_modifier_id IS 'ИД последнего изменившего';
COMMENT ON COLUMN sui_bi.users.name IS 'ФИО';
COMMENT ON COLUMN sui_bi.users.username IS 'Логин';
COMMENT ON COLUMN sui_bi.users.email IS 'Адрес электронной почты';
COMMENT ON COLUMN sui_bi.users.password IS 'Пароль (хэш)';
COMMENT ON COLUMN sui_bi.users.deleted IS 'Признак удаления';

------------------------------------------------------------------------------------------------------------------------
-- Table sui_bi.engines ------------------------------------------------------------------------------------------------
------------------------------------------------------------------------------------------------------------------------

CREATE TABLE sui_bi.engines
(
    id   BIGINT NOT NULL PRIMARY KEY,
    code TEXT   NOT NULL,
    name TEXT   NOT NULL,
    CONSTRAINT engines_code_unique UNIQUE (code),
    CONSTRAINT engines_name_unique UNIQUE (name)
);

COMMENT ON TABLE sui_bi.engines IS 'Справочник: Поддерживаемые типы баз данных';
COMMENT ON COLUMN sui_bi.engines.id IS 'ИД записи (PK)';
COMMENT ON COLUMN sui_bi.engines.code IS 'Код';
COMMENT ON COLUMN sui_bi.engines.name IS 'Наименование';

INSERT INTO sui_bi.engines(id, code, name) VALUES (1, 'postgresql', 'PostgreSQL');

------------------------------------------------------------------------------------------------------------------------
-- Table sui_bi.databases ----------------------------------------------------------------------------------------------
------------------------------------------------------------------------------------------------------------------------

CREATE TABLE sui_bi.databases
(
    id                    BIGSERIAL   NOT NULL PRIMARY KEY,
    -- audit
    created               TIMESTAMPTZ NOT NULL DEFAULT now(),
    creator_id            BIGINT REFERENCES sui_bi.users (id),
    last_modified         TIMESTAMPTZ NOT NULL DEFAULT now(),
    last_modifier_id      BIGINT REFERENCES sui_bi.users (id),
    -- payload
    engine_id             BIGINT      NOT NULL REFERENCES sui_bi.engines (id),
    name                  TEXT        NOT NULL,
    description           TEXT,
    connection_details    JSON        NOT NULL,
    timezone              TEXT        NOT NULL,
    is_fully_synchronized BOOLEAN     NOT NULL,
    CONSTRAINT databases_name_unique UNIQUE (name)
);

COMMENT ON TABLE sui_bi.databases IS 'Подлюченные базы данных';
COMMENT ON COLUMN sui_bi.databases.id IS 'ИД записи (PK)';
COMMENT ON COLUMN sui_bi.databases.created IS 'Дата и время создания';
COMMENT ON COLUMN sui_bi.databases.creator_id IS 'ИД создателя';
COMMENT ON COLUMN sui_bi.databases.last_modified IS 'Дата и время последнего изменения';
COMMENT ON COLUMN sui_bi.databases.last_modifier_id IS 'ИД последнего изменившего';
COMMENT ON COLUMN sui_bi.databases.engine_id IS 'ИД типа';
COMMENT ON COLUMN sui_bi.databases.name IS 'Наименование';
COMMENT ON COLUMN sui_bi.databases.description IS 'Описание';
COMMENT ON COLUMN sui_bi.databases.connection_details IS 'Детали подключения';
COMMENT ON COLUMN sui_bi.databases.timezone IS 'Tаймзона';
COMMENT ON COLUMN sui_bi.databases.is_fully_synchronized IS 'Синхронизирована и готова к использованию';

------------------------------------------------------------------------------------------------------------------------
-- Table sui_bi.tables -------------------------------------------------------------------------------------------------
------------------------------------------------------------------------------------------------------------------------

CREATE TABLE sui_bi.tables
(
    id               BIGSERIAL   NOT NULL PRIMARY KEY,
    -- audit
    created          TIMESTAMPTZ NOT NULL DEFAULT now(),
    creator_id       BIGINT REFERENCES sui_bi.users (id),
    last_modified    TIMESTAMPTZ NOT NULL DEFAULT now(),
    last_modifier_id BIGINT REFERENCES sui_bi.users (id),
    -- payload
    database_id      BIGINT      NOT NULL REFERENCES sui_bi.databases (id) ON DELETE CASCADE,
    table_schema     TEXT,
    table_name       TEXT        NOT NULL,
    table_type       TEXT,
    CONSTRAINT tables_database_id_table_schema_table_name_unique UNIQUE (database_id, table_schema, table_name)
);

CREATE INDEX tables_database_id_index ON sui_bi.tables (database_id);

COMMENT ON TABLE sui_bi.tables IS 'Таблицы (метаданные)';
COMMENT ON COLUMN sui_bi.tables.id IS 'ИД записи (PK)';
COMMENT ON COLUMN sui_bi.tables.created IS 'Дата и время создания';
COMMENT ON COLUMN sui_bi.tables.creator_id IS 'ИД создателя';
COMMENT ON COLUMN sui_bi.tables.last_modified IS 'Дата и время последнего изменения';
COMMENT ON COLUMN sui_bi.tables.last_modifier_id IS 'ИД последнего изменившего';
COMMENT ON COLUMN sui_bi.tables.database_id IS 'ИД базы данных';
COMMENT ON COLUMN sui_bi.tables.table_schema IS 'Схема таблицы в БД';
COMMENT ON COLUMN sui_bi.tables.table_name IS 'Имя таблицы в БД';
COMMENT ON COLUMN sui_bi.tables.table_type IS 'Тип (TABLE, VIEW, MATERIALIZED VIEW и пр.)';

------------------------------------------------------------------------------------------------------------------------
-- Table sui_bi.column_types -------------------------------------------------------------------------------------------
------------------------------------------------------------------------------------------------------------------------

CREATE TABLE sui_bi.column_types
(
    id          BIGINT NOT NULL PRIMARY KEY,
    code        TEXT   NOT NULL,
    description TEXT   NOT NULL,
    CONSTRAINT column_types_code_unique UNIQUE (code)
);

COMMENT ON TABLE sui_bi.column_types IS 'Справочник: Типов колонок';
COMMENT ON COLUMN sui_bi.column_types.id IS 'ИД записи (PK)';
COMMENT ON COLUMN sui_bi.column_types.code IS 'Код';
COMMENT ON COLUMN sui_bi.column_types.description IS 'Описание';

INSERT INTO sui_bi.column_types(id, code, description)
VALUES (1, 'boolean', 'Логическое значение'),
       (2, 'integer', 'Целое число'),
       (3, 'decimal', 'Десятичное число'),
       (4, 'date', 'Дата'),
       (5, 'timestamp', 'Дата и время'),
       (6, 'time', 'Время'),
       (7, 'string', 'Строка'),
       (8, 'binary', 'Бинарное значение');

------------------------------------------------------------------------------------------------------------------------
-- Table sui_bi.columns ------------------------------------------------------------------------------------------------
------------------------------------------------------------------------------------------------------------------------

CREATE TABLE sui_bi.columns
(
    id               BIGSERIAL   NOT NULL PRIMARY KEY,
    -- audit
    created          TIMESTAMPTZ NOT NULL DEFAULT now(),
    creator_id       BIGINT REFERENCES sui_bi.users (id),
    last_modified    TIMESTAMPTZ NOT NULL DEFAULT now(),
    last_modifier_id BIGINT REFERENCES sui_bi.users (id),
    -- payload
    table_id         BIGINT      NOT NULL REFERENCES sui_bi.tables (id) ON DELETE CASCADE,
    column_name      TEXT        NOT NULL,
    column_type_id   BIGINT      NOT NULL REFERENCES sui_bi.column_types (id),
    raw_column_type  TEXT        NOT NULL,
    is_nullable      BOOLEAN     NOT NULL,
    CONSTRAINT columns_table_id_column_name_unique UNIQUE (table_id, column_name)
);

CREATE INDEX columns_table_id_index ON sui_bi.columns (table_id);

COMMENT ON TABLE sui_bi.columns IS 'Колонки (метаданные)';
COMMENT ON COLUMN sui_bi.columns.id IS 'ИД записи (PK)';
COMMENT ON COLUMN sui_bi.columns.created IS 'Дата и время создания';
COMMENT ON COLUMN sui_bi.columns.creator_id IS 'ИД создателя';
COMMENT ON COLUMN sui_bi.columns.last_modified IS 'Дата и время последнего изменения';
COMMENT ON COLUMN sui_bi.columns.last_modifier_id IS 'ИД последнего изменившего';
COMMENT ON COLUMN sui_bi.columns.table_id IS 'ИД таблицы';
COMMENT ON COLUMN sui_bi.columns.column_name IS 'Имя колонки в БД';
COMMENT ON COLUMN sui_bi.columns.column_type_id IS 'ИД типа';
COMMENT ON COLUMN sui_bi.columns.raw_column_type IS 'Сырой тип';
COMMENT ON COLUMN sui_bi.columns.is_nullable IS 'Может ли значение быть пустым?';
