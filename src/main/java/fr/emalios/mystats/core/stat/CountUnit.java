package fr.emalios.mystats.core.stat;

import oshi.util.tuples.Pair;

public enum CountUnit {

    ITEM(""),
    STACK(""),
    MB("mb"),
    B("b"),
    FE("");


    private final String display;

    CountUnit(String display) {
        this.display = display;
    }

    @Override
    public String toString() {
        return this.display;
    }

    public static Pair<Float, CountUnit> simplify(float value, CountUnit unit) {
        if(value < 1000) return new Pair<>(value, unit);
        else return new Pair<>(Math.round(value/1000*10)/10.0f, switch (unit) {
            case ITEM ->  ITEM;
            case STACK -> STACK;
            case MB -> B;
            case B -> B;
            case FE -> FE;
        });
    }
}
