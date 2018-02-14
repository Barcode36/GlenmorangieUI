package com.ruddlesdin;

/**
 * Created by p_ruddlesdin on 21/03/2017.
 */
import javafx.beans.property.SimpleStringProperty;

public class Warenr {

    private final SimpleStringProperty warenr;

    public Warenr(String warenr) {
        super();
        this.warenr = new SimpleStringProperty(warenr);

    }

    public SimpleStringProperty getWarenr() {
        return warenr;
    }
}
