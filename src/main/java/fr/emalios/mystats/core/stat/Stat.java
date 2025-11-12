package fr.emalios.mystats.core.stat;


import java.time.Instant;
import java.util.Objects;
import java.util.UUID;

/**
 * Représente une donnée de statistique abstraite et indépendante de Minecraft.
 * Exemple : "inséré 5x 'minecraft:iron_ingot' dans 'log_chest#123' à 12:34:56"
 */

public class Stat {

    /** Type de statistique (INSERT, EXTRACT, BREAK, PLACE, etc.) */
    private final StatType type;

    /** Identifiant logique de ce qui est mesuré (ex: "minecraft:iron_ingot") */
    private final String targetId;

    /** Identifiant du propriétaire de la stat (ex: joueur, bloc, dimension, etc.) */
    private final UUID ownerId;

    private final String sourceId;

    /** Valeur quantitative de la mesure */
    private final float count;

    /** Unité (items, fluid, energy, etc.) */
    private final Unit unit;

    /** Timestamp ISO pour la persistance / analyse temporelle */
    private final Instant timestamp;

    public Stat(
            StatType type,
            String targetId,
            UUID ownerId,
            String sourceId,
            float count,
            Unit unit,
            Instant timestamp
    ) {
        this.type = Objects.requireNonNull(type);
        this.targetId = Objects.requireNonNull(targetId);
        this.ownerId = ownerId;
        this.sourceId = sourceId;
        this.count = count;
        this.unit = Objects.requireNonNull(unit);
        this.timestamp = timestamp != null ? timestamp : Instant.now();
    }

    // --- Getters ---
    public StatType getType() { return type; }
    public String getTargetId() { return targetId; }
    public UUID getOwnerId() { return ownerId; }
    public String getSourceId() { return sourceId; }
    public float getCount() { return count; }
    public Unit getUnit() { return unit; }
    public Instant getTimestamp() { return timestamp; }

    // --- Utilitaires ---
    @Override
    public String toString() {
        return "Stat{" +
                "type=" + type +
                ", targetId='" + targetId + '\'' +
                ", ownerId=" + ownerId +
                ", sourceId='" + sourceId + '\'' +
                ", count=" + count +
                ", unit=" + unit +
                ", timestamp=" + timestamp +
                '}';
    }
}
