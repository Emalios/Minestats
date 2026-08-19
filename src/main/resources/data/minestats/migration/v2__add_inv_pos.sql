PRAGMA foreign_keys = OFF;

-- 1. Sauvegarder les positions
CREATE TEMP TABLE inventories_pos_backup (
                                             inventory_id INTEGER,
                                             world TEXT,
                                             x INTEGER,
                                             y INTEGER,
                                             z INTEGER
);

INSERT INTO inventories_pos_backup (inventory_id, world, x, y, z)
SELECT id, world, x, y, z FROM inventories;

-- 2. Créer la nouvelle table inventories
CREATE TABLE inventories_new (
                                 id INTEGER PRIMARY KEY AUTOINCREMENT,
                                 type TEXT NOT NULL,
                                 created_at DATETIME DEFAULT CURRENT_TIMESTAMP
);

-- 3. Copier les données inventories
INSERT INTO inventories_new (id, type, created_at)
SELECT id, type, created_at FROM inventories;

-- 4. Supprimer l'ancienne table
DROP TABLE inventories;

-- 5. Renommer
ALTER TABLE inventories_new RENAME TO inventories;

-- 6. Créer inventory_pos (FK valide)
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

-- 7. Restaurer les positions
INSERT INTO inventory_pos (inventory_id, world, x, y, z)
SELECT inventory_id, world, x, y, z
FROM inventories_pos_backup;

DROP TABLE inventories_pos_backup;

PRAGMA foreign_keys = ON;
