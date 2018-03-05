package com.ruddlesdin;

/**
 * Created by p_ruddlesdin on 21/03/2017.
 */
import javafx.beans.property.SimpleStringProperty;
import javafx.beans.property.SimpleIntegerProperty;

public class Products {

    private final SimpleStringProperty warenr;
    private final SimpleStringProperty labeltext;
    private final SimpleStringProperty labeltext2;
    private final SimpleStringProperty servicedescription;
    private final SimpleStringProperty barcodec;
    private final SimpleStringProperty barcoded;
    private final SimpleIntegerProperty gramweightperdpack;
    private final SimpleStringProperty labeltext3;
    private final SimpleStringProperty barcodeclabel;
    private final SimpleStringProperty barcodedlabel;
    private final SimpleStringProperty barcodetlabel;
    private final SimpleIntegerProperty dtocratio;
    private final SimpleIntegerProperty dtolayerratio;
    private final SimpleIntegerProperty ttodratio;


    public Products(String warenr, String labeltext, String labeltext2, String servicedescription, String barcodec, String barcoded, Integer gramweightperdpack, String labeltext3, String barcodeclabel, String barcodedlabel, String barcodetlabel, Integer dtocratio, Integer dtolayerratio, Integer ttodratio) {
        super();
        this.warenr = new SimpleStringProperty(warenr);
        this.labeltext = new SimpleStringProperty(labeltext);
        this.labeltext2 = new SimpleStringProperty(labeltext2);
        this.servicedescription = new SimpleStringProperty(servicedescription);
        this.barcodec = new SimpleStringProperty(barcodec);
        this.barcoded = new SimpleStringProperty(barcoded);
        this.gramweightperdpack = new SimpleIntegerProperty(gramweightperdpack);
        this.labeltext3 = new SimpleStringProperty(labeltext3);
        this.barcodeclabel = new SimpleStringProperty(barcodeclabel);
        this.barcodedlabel = new SimpleStringProperty(barcodedlabel);
        this.barcodetlabel = new SimpleStringProperty(barcodetlabel);
        this.dtocratio = new SimpleIntegerProperty(dtocratio);
        this.dtolayerratio = new SimpleIntegerProperty(dtolayerratio);
        this.ttodratio = new SimpleIntegerProperty(ttodratio);
    }


    public SimpleStringProperty getWarenr() {
        return warenr;
    }


    public SimpleStringProperty getLabeltext() {
        return labeltext;
    }


    public SimpleStringProperty getLabeltext2() {
        return labeltext2;
    }


    public SimpleStringProperty getServicedescription() {
        return servicedescription;
    }


    public SimpleStringProperty getBarcodec() {
        return barcodec;
    }


    public SimpleStringProperty getBarcoded() {
        return barcoded;
    }


    public SimpleIntegerProperty getGramweightperdpack() {
        return gramweightperdpack;
    }


    public SimpleStringProperty getLabeltext3() {
        return labeltext3;
    }


    public SimpleStringProperty getBarcodeclabel() {
        return barcodeclabel;
    }


    public SimpleStringProperty getBarcodedlabel() {
        return barcodedlabel;
    }


    public SimpleStringProperty getBarcodetlabel() {
        return barcodetlabel;
    }


    public SimpleIntegerProperty getDtocratio() {
        return dtocratio;
    }


    public SimpleIntegerProperty getDtolayerratio() {
        return dtolayerratio;
    }


    public SimpleIntegerProperty getTtodratio() {
        return ttodratio;
    }
}
