package fr.esipe.elevatorsim.model;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

/**
 * Un étage du bâtiment (0 = rez-de-chaussée sans résident).
 */
public class Floor {

    private final int index;
    private final List<Resident> residents = new ArrayList<>();

    public Floor(int index) {
        this.index = index;
    }

    public int getIndex() {
        return index;
    }

    public void addResident(Resident resident) {
        residents.add(resident);
    }

    public List<Resident> getResidents() {
        return Collections.unmodifiableList(residents);
    }
}
