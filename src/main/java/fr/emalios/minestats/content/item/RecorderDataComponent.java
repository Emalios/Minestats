package fr.emalios.minestats.content.item;


public class RecorderDataComponent {

    public enum RecorderMode {
        ADD,
        REMOVE,
        DEBUG;
        //VIEW;

        public RecorderMode next() {
            RecorderMode[] values = values();
            return values[(this.ordinal() + 1) % values.length];
        }

    }

    public record RecorderData(RecorderMode mode) {}

}
