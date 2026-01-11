PRAGMA foreign_keys = OFF;

CREATE TABLE inventory_pos (
                               inventory_id INTEGER NOT NULL,
                               world TEXT NOT NULL,
                               x INTEGER NOT NULL,
                               y INTEGER NOT NULL,
                               z INTEGER NOT NULL,
                               PRIMARY KEY (world, x, y, z),
                               FOREIGN KEY (inventory_id)
                                   REFERENCES inventories(id)
                                   ON DELETE CASCADE
);

INSERT INTO inventory_pos (inventory_id, world, x, y, z)
SELECT id, world, x, y, z FROM inventories;

CREATE TABLE inventories_new (
                                 id INTEGER PRIMARY KEY AUTOINCREMENT,
                                 type TEXT NOT NULL,
                                 created_at DATETIME DEFAULT CURRENT_TIMESTAMP
);

INSERT INTO inventories_new (id, type, created_at)
SELECT id, type, created_at FROM inventories;

DROP TABLE inventories;
ALTER TABLE inventories_new RENAME TO inventories;

PRAGMA foreign_keys = ON;
