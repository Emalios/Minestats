package fr.emalios.minestats.api.models.inventory;

import java.util.Collection;
import java.util.Objects;

public interface IPosition {

    public int getX();

    public int getY();

    public int getZ();

    public String getWorld();

    public Collection<IPosition> getAdjacentPositions();

}
