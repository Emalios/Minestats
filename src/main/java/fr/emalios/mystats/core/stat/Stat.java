package fr.emalios.mystats.core.stat;


import java.time.Instant;
import java.util.Objects;
import java.util.UUID;

/**
 * Représente une donnée de statistique abstraite et indépendante de Minecraft.
 */

public class Stat {

    /** Type de statistique (ITEM, FLUID, ...) */
    private final StatType type;

    /** Identifiant logique de ce qui est mesuré (ex: "minecraft:iron_ingot") */
    private final String targetId;

    /** Valeur quantitative de la mesure */
    private final int count;

    /** Unité (items, fluid, energy, etc.) */
    private final Unit unit;

    public Stat(
            StatType type,
            String targetId,
            int count,
            Unit unit
    ) {
        this.type = Objects.requireNonNull(type);
        this.targetId = Objects.requireNonNull(targetId);
        this.count = count;
        this.unit = Objects.requireNonNull(unit);
    }

    // --- Getters ---
    public StatType getType() { return type; }
    public String getTargetId() { return targetId; }
    public int getCount() { return count; }
    public Unit getUnit() { return unit; }

    public Stat mergeWith(Stat stat) {
        if(!this.targetId.equals(stat.getTargetId())) return this; //might throw an exception
        assert this.unit == stat.getUnit();
        return new Stat(this.type, this.targetId, this.count + stat.getCount(), this.unit);
    }

    // --- Utilitaires ---
    @Override
    public String toString() {
        return "Stat{" +
                "type=" + type +
                ", targetId='" + targetId + '\'' +
                ", count=" + count +
                ", unit=" + unit +
                '}';
    }
}
