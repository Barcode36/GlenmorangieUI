package com.ruddlesdin;

/**
 * Created by p_ruddlesdin on 21/03/2017.
 */
import javafx.beans.property.SimpleIntegerProperty;
import javafx.beans.property.SimpleStringProperty;

public class Orders {
    private final SimpleStringProperty status;
    private final SimpleStringProperty startDateTime;
    private final SimpleStringProperty extra1;
    private final SimpleStringProperty batchLotNr;
    private final SimpleStringProperty wareNr;
    private final SimpleStringProperty serviceDescription;
    private final SimpleStringProperty productionLineNr;
    private final SimpleIntegerProperty orderProductionNr;

    Orders(String status, String startDateTime, String extra1, String batchLotNr, String wareNr, String serviceDescription, String productionLineNr, int orderProductionNr) {
        this.status = new SimpleStringProperty(status);
        this.startDateTime = new SimpleStringProperty(startDateTime);
        this.extra1 = new SimpleStringProperty(extra1);
        this.batchLotNr = new SimpleStringProperty(batchLotNr);
        this.wareNr = new SimpleStringProperty(wareNr);
        this.serviceDescription = new SimpleStringProperty(serviceDescription);
        this.productionLineNr = new SimpleStringProperty(productionLineNr);
        this.orderProductionNr = new SimpleIntegerProperty((orderProductionNr));
    }

    public String getStatus() {
        return status.get();
    }

    public String getStartDateTime() {return startDateTime.get(); }

    public String getExtra1() {
        return extra1.get();
    }

    public String getBatchLotNr() {
        return batchLotNr.get();
    }

    public String getWareNr() {
        return wareNr.get();
    }

    public String getServiceDescription() {
        return serviceDescription.get();
    }

    public String getProductionLineNr() {
        return productionLineNr.get();
    }

    int getOrderProductionNr() {return orderProductionNr.get();}
}
