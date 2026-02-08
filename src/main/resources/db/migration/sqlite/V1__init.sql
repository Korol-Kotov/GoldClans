-- ===============================
-- Migration: Clans system tables (SQLite)
-- ===============================

-- -------------------------------
-- clans — таблица кланов
-- -------------------------------
CREATE TABLE clans
(
    id              INTEGER PRIMARY KEY AUTOINCREMENT,
    name            TEXT     NOT NULL,
    leader_uuid     TEXT     NOT NULL,
    home_location   TEXT NULL,
    level           INTEGER  NOT NULL DEFAULT 1,
    storage_slots   INTEGER  NOT NULL DEFAULT 0,
    next_level_info TEXT     NOT NULL,
    created_at      DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP
);

CREATE UNIQUE INDEX idx_clans_name
    ON clans (name);

CREATE INDEX idx_clans_leader
    ON clans (leader_uuid);

CREATE INDEX idx_clans_level
    ON clans (level);

CREATE INDEX idx_clans_storage
    ON clans (storage_slots);

-- -------------------------------
-- clan_members — участники кланов
-- -------------------------------
CREATE TABLE clan_members
(
    player_uuid TEXT PRIMARY KEY,
    player_name TEXT     NOT NULL,
    clan_id     TEXT NULL,
    role        INTEGER NULL, -- 0 - member, 1 - moderator, 2 - leader
    joined_at   DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP
);

CREATE INDEX idx_clan_members_clan
    ON clan_members (clan_id);

CREATE INDEX idx_clan_members_role
    ON clan_members (role);

-- -------------------------------
-- clan_storage — хранилище кланов
-- -------------------------------
CREATE TABLE clan_storage
(
    clan_id   TEXT    NOT NULL,
    slot      INTEGER NOT NULL,
    item_data TEXT    NOT NULL,

    PRIMARY KEY (clan_id, slot)
);

CREATE INDEX idx_clan_storage_clan
    ON clan_storage (clan_id);