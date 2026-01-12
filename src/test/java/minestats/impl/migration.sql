PRAGMA foreign_keys = ON;

CREATE TABLE IF NOT EXISTS players (
                                       id INTEGER PRIMARY KEY AUTOINCREMENT,
                                       name TEXT NOT NULL UNIQUE,
                                       created_at DATETIME DEFAULT CURRENT_TIMESTAMP
);

CREATE TABLE IF NOT EXISTS inventory_snapshots (
                                                   id INTEGER PRIMARY KEY AUTOINCREMENT,
                                                   inventory_id INTEGER NOT NULL,
                                                   timestamp INTEGER NOT NULL,
                                                   FOREIGN KEY (inventory_id) REFERENCES inventories(id) ON DELETE CASCADE
);