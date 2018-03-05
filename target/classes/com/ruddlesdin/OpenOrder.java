package com.ruddlesdin;

/**
 * Created by p_ruddlesdin on 24/03/2017.
 */

// POJO (Plain Old Java Object)
import javafx.beans.property.SimpleStringProperty;

public class OpenOrder {

    private final SimpleStringProperty warenr;
    private final SimpleStringProperty orderProductionNr;
    private final SimpleStringProperty extra1;
    private final SimpleStringProperty batchLotNr;
    private final SimpleStringProperty repack;
    private final SimpleStringProperty repackDate;
    private final SimpleStringProperty repackBatch;
    private final SimpleStringProperty repackYear;
    private final SimpleStringProperty status;
    private final SimpleStringProperty productionLineNr;
    private final SimpleStringProperty startDateTime;

    private final SimpleStringProperty labeltext;
    private final SimpleStringProperty labeltext2;
    private final SimpleStringProperty servicedescription;
    private final SimpleStringProperty barcodec;
    private final SimpleStringProperty barcoded;
    private final SimpleStringProperty gramweightperdpack;
    private final SimpleStringProperty labeltext3;
    private final SimpleStringProperty barcodeclabel;
    private final SimpleStringProperty barcodedlabel;
    private final SimpleStringProperty barcodetlabel;
    private final SimpleStringProperty dtocratio;
    private final SimpleStringProperty dtolayerratio;
    private final SimpleStringProperty ttodratio;

    OpenOrder(String warenr, String orderProductionNr, String extra1, String batchLotNr, String repack, String repackDate, String repackBatch, String SSCCYear, String status, String productionLineNr, String startDateTime, String labeltext, String labeltext2, String servicedescription, String barcodec, String barcoded, String gramweightperdpack, String labeltext3, String barcodeclabel, String barcodedlabel, String barcodetlabel, String dtocratio, String dtolayerratio, String ttodratio) {
        this.warenr = new SimpleStringProperty(warenr);
        this.orderProductionNr = new SimpleStringProperty(orderProductionNr);
        this.extra1 = new SimpleStringProperty(extra1);
        this.batchLotNr = new SimpleStringProperty(batchLotNr);
        this.repack = new SimpleStringProperty(repack);
        this.repackDate = new SimpleStringProperty(repackDate);
        this.repackBatch = new SimpleStringProperty(repackBatch);
        this.repackYear = new SimpleStringProperty(SSCCYear);
        this.status = new SimpleStringProperty(status);
        this.productionLineNr = new SimpleStringProperty(productionLineNr);
        this.startDateTime = new SimpleStringProperty(startDateTime);
        this.labeltext = new SimpleStringProperty(labeltext);
        this.labeltext2 = new SimpleStringProperty(labeltext2);
        this.servicedescription = new SimpleStringProperty(servicedescription);
        this.barcodec = new SimpleStringProperty(barcodec);
        this.barcoded = new SimpleStringProperty(barcoded);
        this.gramweightperdpack = new SimpleStringProperty(gramweightperdpack);
        this.labeltext3 = new SimpleStringProperty(labeltext3);
        this.barcodeclabel = new SimpleStringProperty(barcodeclabel);
        this.barcodedlabel = new SimpleStringProperty(barcodedlabel);
        this.barcodetlabel = new SimpleStringProperty(barcodetlabel);
        this.dtocratio = new SimpleStringProperty(dtocratio);
        this.dtolayerratio = new SimpleStringProperty(dtolayerratio);
        this.ttodratio = new SimpleStringProperty(ttodratio);
    }

    String getWarenr() {
        return warenr.get();
    }

    String getOrderProductionNr() {
        return orderProductionNr.get();
    }

    String getExtra1() {
        return extra1.get();
    }

    String getBatchLotNr() {
        return batchLotNr.get();
    }

    String getRepack() { return repack.get(); }

    String getRepackDate() {
        return repackDate.get();
    }

    String getRepackBatch() { return repackBatch.get(); }

    String getRepackYear() {
        return repackYear.get();
    }

    String getStatus() {
        return status.get();
    }

    String getProductionLineNr() {
        return productionLineNr.get();
    }

    public String getStartDateTime() {
        return startDateTime.get();
    }

    String getLabeltext() {
        return labeltext.get();
    }

    String getLabeltext2() {
        return labeltext2.get();
    }

    String getServicedescription() {
        return servicedescription.get();
    }

    String getBarcodec() {
        return barcodec.get();
    }

    String getBarcoded() {
        return barcoded.get();
    }

    String getGramweightperdpack() {
        return gramweightperdpack.get();
    }

    String getLabeltext3() {
        return labeltext3.get();
    }

    String getBarcodeclabel() {
        return barcodeclabel.get();
    }

    String getBarcodedlabel() {
        return barcodedlabel.get();
    }

    String getBarcodetlabel() {
        return barcodetlabel.get();
    }

    String getDtocratio() {
        return dtocratio.get();
    }

    String getDtolayerratio() {
        return dtolayerratio.get();
    }

    String getTtodratio() {
        return ttodratio.get();
    }
}
