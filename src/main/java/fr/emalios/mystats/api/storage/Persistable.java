package fr.emalios.mystats.api.storage;

public abstract class Persistable {

    private Integer id;

    public final int getId() {
        if (id == null) {
            throw new IllegalStateException(
                    getClass().getSimpleName() + " not persisted"
            );
        }
        return id;
    }

    public final boolean isPersisted() {
        return id != null;
    }

    public final void assignId(int id) {
        if (this.id != null) {
            throw new IllegalStateException(
                    getClass().getSimpleName() + " already persisted"
            );
        }
        this.id = id;
    }
}

