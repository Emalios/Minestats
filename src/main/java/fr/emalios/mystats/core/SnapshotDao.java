package fr.emalios.mystats.core;

/**
 * A snapshot is an inventory content at a t instant.
 */
public class SnapshotDao {

    public static class Item {
        public final String name;
        public final int count;

        public Item(String name, int count) {
            this.name = name;
            this.count = count;
        }
    }



}
