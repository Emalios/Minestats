package fr.emalios.mystats.api.models.record;


import fr.emalios.mystats.helper.Pair;

public enum CountUnit {

    ITEM(""),
    MB("mB"),
    B("B");


    private final String display;

    CountUnit(String display) {
        this.display = display;
    }

    @Override
    public String toString() {
        return this.display;
    }

    public static Pair<Float, CountUnit> simplify(float value, CountUnit unit) {
        if(value < 1000 && value > -1000) return new Pair<>(value, unit);
        else return new Pair<>(Math.round(value/1000*10)/10.0f, switch (unit) {
            case ITEM ->  ITEM;
            case MB -> B;
            case B -> B;
        });
    }
}
