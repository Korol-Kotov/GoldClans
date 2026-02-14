-- ===============================
-- Migration: Clans system tables
-- ===============================

SET NAMES utf8mb4;
SET time_zone = '+00:00';

-- -------------------------------
-- clans — таблица кланов
-- -------------------------------
CREATE TABLE clans
(
    id              INT AUTO_INCREMENT PRIMARY KEY,
    name            TEXT        NOT NULL,
    leader_uuid     VARCHAR(36) NOT NULL,
    home_location   VARCHAR(128) NULL,
    level           INT         NOT NULL DEFAULT 1,
    storage_slots   INT         NOT NULL DEFAULT 0,
    next_level_info TEXT        NOT NULL,
    created_at      TIMESTAMP   NOT NULL DEFAULT CURRENT_TIMESTAMP,

    INDEX           idx_clans_leader (leader_uuid)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4;

-- -------------------------------
-- clan_members — участники кланов
-- -------------------------------
CREATE TABLE clan_members
(
    player_uuid VARCHAR(36) PRIMARY KEY,
    player_name VARCHAR(16) NOT NULL,
    clan_id     INT NULL,
    role        TINYINT NULL COMMENT '0 - member, 1 - moderator, 2 - leader',
    joined_at   TIMESTAMP   NOT NULL DEFAULT CURRENT_TIMESTAMP,

    INDEX       idx_clan_members_clan (clan_id)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4;

-- -------------------------------
-- clan_storage — хранилище кланов
-- -------------------------------
CREATE TABLE clan_storage
(
    clan_id   INT  NOT NULL,
    slot      INT  NOT NULL,
    item_data TEXT NOT NULL,

    PRIMARY KEY (clan_id, slot),
    FOREIGN KEY (clan_id) REFERENCES clans (id)
        ON DELETE CASCADE
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4;