PRAGMA foreign_keys = ON;

CREATE TABLE IF NOT EXISTS players (
                                       id INTEGER PRIMARY KEY AUTOINCREMENT,
                                       name TEXT NOT NULL UNIQUE,
                                       created_at DATETIME DEFAULT CURRENT_TIMESTAMP
);

CREATE TABLE IF NOT EXISTS inventories (
                                           id INTEGER PRIMARY KEY AUTOINCREMENT,
                                           world TEXT NOT NULL,
                                           x INTEGER NOT NULL,
                                           y INTEGER NOT NULL,
                                           z INTEGER NOT NULL,
                                           type TEXT NOT NULL,
                                           created_at DATETIME DEFAULT CURRENT_TIMESTAMP,
                                           UNIQUE(world, x, y, z)
);

CREATE TABLE IF NOT EXISTS inventory_snapshots (
                                                   id INTEGER PRIMARY KEY AUTOINCREMENT,
                                                   inventory_id INTEGER NOT NULL,
                                                   timestamp INTEGER NOT NULL,
                                                   FOREIGN KEY (inventory_id) REFERENCES inventories(id) ON DELETE CASCADE
);

CREATE TABLE IF NOT EXISTS snapshot_items (
                                              id INTEGER PRIMARY KEY AUTOINCREMENT,
                                              snapshot_id INTEGER NOT NULL,
                                              item_name TEXT NOT NULL,
                                              count REAL NOT NULL,
                                              stat_type TEXT NOT NULL,
                                              countUnit TEXT NOT NULL,
                                              FOREIGN KEY (snapshot_id) REFERENCES inventory_snapshots(id) ON DELETE CASCADE
);

CREATE TABLE IF NOT EXISTS player_inventories (
                                                  player_id INTEGER NOT NULL,
                                                  inventory_id INTEGER NOT NULL,
                                                  added_at DATETIME DEFAULT CURRENT_TIMESTAMP,
                                                  PRIMARY KEY (player_id, inventory_id),
                                                  FOREIGN KEY (player_id) REFERENCES players(id) ON DELETE CASCADE,
                                                  FOREIGN KEY (inventory_id) REFERENCES inventories(id) ON DELETE CASCADE
);

CREATE TABLE IF NOT EXISTS schema_version (
                                              version INTEGER NOT NULL
);

INSERT INTO schema_version(version)
SELECT 0
WHERE NOT EXISTS (SELECT 1 FROM schema_version);

CREATE INDEX IF NOT EXISTS idx_inventory_snapshots_inventory
    ON inventory_snapshots(inventory_id);

CREATE INDEX IF NOT EXISTS idx_snapshot_items_snapshot
    ON snapshot_items(snapshot_id);