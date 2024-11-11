CREATE TABLE IF NOT EXISTS permissions(
    permission VARCHAR(128) NOT NULL, -- if the permission exceeds 128 characters im gonna kms
    PRIMARY KEY (permission)
);

CREATE TABLE IF NOT EXISTS perm_groups(
    name VARCHAR(64) UNIQUE NOT NULL, -- if a group exceeds 64 characters im gonna kms
    default_group BOOLEAN NOT NULL DEFAULT FALSE,
    PRIMARY KEY (name)
);

CREATE TABLE IF NOT EXISTS group_permissions(
    name VARCHAR(64) NOT NULL,
    permission VARCHAR(128) NOT NULL,
    FOREIGN KEY (name) REFERENCES perm_groups(name),
    FOREIGN KEY (permission) REFERENCES permissions(permission),
    PRIMARY KEY (name, permission)
);

CREATE TABLE IF NOT EXISTS group_metadata(
    name VARCHAR(64) NOT NULL,
    prefix TEXT NULL,
    suffix TEXT NULL,
    weight INT NOT NULL DEFAULT 0,
    FOREIGN KEY (name) REFERENCES perm_groups(name),
    PRIMARY KEY (name)
);

CREATE TABLE IF NOT EXISTS perm_players(
    uuid CHAR(36) UNIQUE NOT NULL,
    name VARCHAR(16) NOT NULL,
    PRIMARY KEY (uuid)
);

CREATE TABLE IF NOT EXISTS permission_signs(
    id INT NOT NULL AUTO_INCREMENT,
    world VARCHAR(64) NOT NULL,
    x INT NOT NULL,
    y INT NOT NULL,
    z INT NOT NULL,
    PRIMARY KEY (id)
);

CREATE TABLE IF NOT EXISTS player_permissions(
    uuid CHAR(36) NOT NULL,
    permission VARCHAR(128) NOT NULL,
    FOREIGN KEY (uuid) REFERENCES perm_players(uuid),
    FOREIGN KEY (permission) REFERENCES permissions(permission),
    PRIMARY KEY (uuid, permission)
);

CREATE TABLE IF NOT EXISTS player_groups(
    uuid CHAR(36) NOT NULL,
    group_name VARCHAR(64) NOT NULL,
    group_until BIGINT NULL default -1,
    FOREIGN KEY (uuid) REFERENCES perm_players(uuid),
    FOREIGN KEY (group_name) REFERENCES perm_groups(name),
    PRIMARY KEY (uuid, group_name)
);