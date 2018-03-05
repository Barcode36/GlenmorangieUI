package com.ruddlesdin;

/**
 * Created by p_ruddlesdin on 21/03/2017.
 */
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;

import java.sql.*;

public class FirebirdConnect{

    // JDBC driver name and database URL
    private static final String JDBC_DRIVER = "org.firebirdsql.jdbc.FBDriver";
    private String IPAddress = "localhost";
    private String DB_URL = "jdbc:firebirdsql://localhost/C:/Var/db/UTF-8/Glenmorangie/Glenmorangie.FDB?useUnicode=true&characterEncoding=utf8";

    // Database credentials
    private  static final String USER = "SYSDBA";
    private static final String PASS = "masterkey";

    private Connection conn = null;
    private PreparedStatement pstmt = null;
    private ResultSet rs = null;
    private ObservableList<String> wareList = FXCollections.observableArrayList();
    private ObservableList<OpenOrder> list2 = FXCollections.observableArrayList();

    
    
    private String OrderSQL = "";

    private MainController mainController;

    private String calSSCC = "SELECT GEN_ID(CAL_PALLET_SSCC,0) AS CAL_PALLET_SSCC FROM RDB$DATABASE";
    private String flexSSCC = "SELECT GEN_ID(FLEX_PALLET_SSCC,0) AS FLEX_PALLET_SSCC FROM RDB$DATABASE";
    private String finSSCC = "SELECT GEN_ID(FIN_PALLET_SSCC,0) AS FIN_PALLET_SSCC FROM RDB$DATABASE";
    private String minSSCC = "SELECT GEN_ID(MIN_PALLET_SSCC,0) AS MIN_PALLET_SSCC FROM RDB$DATABASE";

    private String caledonian = "Caledonian";
    private String flexiLine = "FlexiLine";
    private String finishing = "Finishing";
    private String miniatures = "Miniatures";
    private String prodLine;
    private String prodLineNr;
    private Logs log;

    FirebirdConnect(MainController mainController, String ipAddress, String dbPath){
        this.mainController = mainController;
        this.IPAddress = ipAddress;
        
        prodLine = mainController.getProductionLine().trim();
        IPAddress = mainController.getIpAddress();
        dbPath = mainController.getDbPath();
        DB_URL = "jdbc:firebirdsql://" + IPAddress + "/" + dbPath + "?useUnicode=true&characterEncoding=utf8";
        log = new Logs();
    }

    public boolean checkConnection() {
        System.out.println("Check Connection");
        try {
            System.out.println("Checking Connection to database...");
            Class.forName(JDBC_DRIVER);
            conn = DriverManager.getConnection(DB_URL, USER, PASS);
            mainController.setConnected(true);
            mainController.dbTestTick();
            conn.close();
            return true;
        } catch (Exception e) {
            System.out.println("Failed to test connect to database...");
            log.appLog("Failed to test connect to database\r\n");
            log.appLog(e.toString()+"\r\n");
            mainController.setConnected(false);
            mainController.dbTestCross();
            return false;
        }

    }

    void FBConnection() {
        try {
            System.out.println("Connecting to database...");
            Class.forName(JDBC_DRIVER);
            conn = DriverManager.getConnection(DB_URL, USER, PASS);
            mainController.setConnected(true);
            mainController.dbTick();
        } catch (Exception e) {
            System.out.println("Failed to connect to database...");
            log.appLog("Failed to connect to database\r\n");
            log.appLog(e.toString()+"\r\n");
            mainController.setConnected(false);
            mainController.dbCross();
        }
    }

    void FBConnectionClose() {
        if(mainController.isConnected()) {
            try {
                pstmt.close();
                conn.close();

            } catch (SQLException e) {
                System.out.println("Failed to close connections");
                log.appLog("Failed to close connections\r\n");
                e.printStackTrace();
                log.appLog(e.toString() + "\r\n");
            }
        }
    }

    ObservableList<Orders> FBSelect(String sqlStatement){
        ObservableList<Orders> list = FXCollections.observableArrayList();
        if(mainController.isConnected()) {
            FBConnection();
            System.out.println("Creating statement...");
            try {
                pstmt = conn.prepareStatement(sqlStatement, ResultSet.TYPE_SCROLL_INSENSITIVE, ResultSet.CONCUR_UPDATABLE);
                rs = pstmt.executeQuery();
            } catch (SQLException e) {
                System.out.println("Failed to prepare statement");
                e.printStackTrace();
                log.appLog("Failed to prepare statement\r\n");
                log.appLog(e.toString() + "\r\n");
            }
            System.out.println("Creating list");
            Orders orders;
            try {
                while (rs.next()) {
                    String status;
                    int sta = rs.getInt("status");
                    switch (sta) {
                    case 6:
                        status = "READY";
                        break;
                    case 7:
                        status = "STARTED";
                        break;
                    case 8:
                        status = "DONE";
                        break;
                    default:
                        status = "UNKNOWN";
                        break;
                    }
                    String line;
                    int lin = rs.getInt("productionLineNr");
                    switch (lin) {
                    case 15:
                        line = "CALEDONIAN";
                        break;
                    case 16:
                        line = "FINISHING";
                        break;
                    case 17:
                        line = "FLEXILINE";
                        break;
                    case 18:
                        line = "MINIATURES";
                        break;
                    default:
                        line = "UNKNOWN";
                        break;
                    }
                    orders = new Orders(status, rs.getString("startDateTime"), rs.getString("extra1"), rs.getString("batchlotnr"), rs.getString("wareNr"), rs.getString("serviceDescription"),
                            line, rs.getInt("orderProductionNr"));
                    list.add(orders);
                }
            } catch (SQLException e) {
                System.out.println("Failed to get resultset");
                e.printStackTrace();
            }
            FBConnectionClose();
        }
        return list;
    }

    public void FBDelete(String sqlStatement){
        FBConnection();
        System.out.println("Creating statement...");
        execute(sqlStatement);
        FBConnectionClose();
    }

    public void FBInsert(String sqlStatement){
        FBConnection();
        System.out.println("Creating statement...");
        execute(sqlStatement);
        FBConnectionClose();
    }

    public void FBUpdate(String sqlStatement){
        FBConnection();
        System.out.println("Creating statement...");
        execute(sqlStatement);
        FBConnectionClose();
    }

    public void FBInsertUpdate(String sqlStatement){
        FBConnection();
        System.out.println("Creating statement...");
        execute(sqlStatement);
        FBConnectionClose();
    }

    String getStatus(String orderNr) {
        int statusNr = 99;
        String status;
        FBConnection();
        String sqlGetStatus = "SELECT STATUS FROM TBLORDERPRODUCTION WHERE ORDERPRODUCTIONNR = " + orderNr;
        execute(sqlGetStatus);
        try {
            while (rs.next()) {
                statusNr = rs.getInt("STATUS");
                System.out.println("Status = " + String.valueOf(statusNr));
            }
        } catch(SQLException e) {
            System.out.println("Failed to get Status");
            e.printStackTrace();

        }
        FBConnectionClose();
        switch (statusNr) {
            case 6:
                status = "READY";
                break;
            case 7:
                status = "STARTED";
                break;
            case 8:
                status = "DONE";
                break;
            default:
                status = "UNKNOWN";
                break;
        }
        return status;
    }

    int stopOrder(String orderNr) {
        FBConnection();
        boolean exists = checkOrderRunning(orderNr);
        if(exists) {
			String updateProdStatus = "UPDATE TBLORDERPRODUCTION SET STATUS = '8' WHERE ORDERPRODUCTIONNR = " + orderNr + " AND STATUS = 7";
			executeUpdate(updateProdStatus);
			System.out.println("Changed state from 7 to 8");
			log.appLog("Changed state from 7 to 8\r\n");
            boolean stillExists = checkOrderRunning(orderNr);
            if(stillExists) {
                System.out.println("Order failed to stop");
                return 105; // Order failed to stop
            } else {
                incrementGenID();
				String insertProdReq = "INSERT INTO tblProductionRequest(ProductionControllerNr,ProductionActionTypeNr, Parameters, WhereClause, SkipFactor, Repetition, RequestDateTime, ExtraValue1, ExtraValue2, ExtraValue3, ExtraValue4, ExtraValue5, EXTRABOOLEAN1, EXTRABOOLEAN2, EXTRABOOLEAN3, EXTRABOOLEAN4, EXTRABOOLEAN5, RECIPIENTKEYNR, RECIPIENTAREA, FORCECHECKREVISION, ORDERPRODUCTIONNR, PRODUCTIONLINENR, EXTRA1, EXTRA2, EXTRA3, WARENR, BATCHLOTNR) " +
						"SELECT ProductionControllerNr, ProductionActionTypeNr, Parameters, WhereClause, SkipFactor, Repetition, CURRENT_TIMESTAMP, null AS ExtraValue1, null AS ExtraValue2, null AS ExtraValue3, null AS ExtraValue4, null AS ExtraValue5, null AS EXTRABOOLEAN1, null AS EXTRABOOLEAN2, null AS EXTRABOOLEAN3, null AS EXTRABOOLEAN4, null AS EXTRABOOLEAN5, null AS RECIPIENTKEYNR, null AS RECIPIENTAREA,'1' AS FORCECHECKREVISION, null AS ORDERPRODUCTIONNR, null AS PRODUCTIONLINENR, null AS EXTRA1, null AS EXTRA2, null AS EXTRA3, null AS WARENR, null AS BATCHLOTNR FROM tblProdLineRequest WHERE PRODUCTIONLINENR = " + prodLineNr + " AND PRODUCTIONACTIONTYPENR = 501";
				executeUpdate(insertProdReq);
				System.out.println("Inserted prod request for stop button");
				log.appLog("Inserted prod request for stop button\r\n");
				String updateActivityState = "UPDATE TBLPRODUCTIONLINE SET ACTIVEORDERPRODUCTIONNR = 0, ACTIVEWARENR = '', ACTIVEBATCHLOTNR = '', ACTIVESERVICEDESCRIPTION = '', ACTIVELABELTEXT = '', ACTIVELABELTEXT2 = '', ACTIVELABELTEXT3 = '' WHERE PRODUCTIONLINENR = " + prodLineNr + ";";
                executeUpdate(updateActivityState);
				System.out.println("Updated activity state after stop");
				log.appLog("Updated activity state after stop\r\n");
			}
            FBConnectionClose();
            return 0; // Order stopped successfully
        } else {
            System.out.println("The order is not running");
            FBConnectionClose();
            return 103; // The order is not running
        }
    }

    public void pressButton(String orderNr, String editSSCC, String type) {
        setSSCC(orderNr,editSSCC);
        list2 = getOrderData(Integer.parseInt(orderNr));
        FBConnection();
        int prodLineNr2 = getProductionLineNr(list2.get(0).getProductionLineNr());
        prodLineNr = Integer.toString(prodLineNr2);
        String insertProdLineReq = "INSERT INTO tblProductionRequest(ProductionControllerNr,ProductionActionTypeNr, Parameters, WhereClause, SkipFactor, Repetition, RequestDateTime, ExtraValue1, ExtraValue2, ExtraValue3, ExtraValue4, ExtraValue5, EXTRABOOLEAN1, EXTRABOOLEAN2, EXTRABOOLEAN3, EXTRABOOLEAN4, EXTRABOOLEAN5, RECIPIENTKEYNR, RECIPIENTAREA, FORCECHECKREVISION, ORDERPRODUCTIONNR, PROCTIONLINRNR, EXTRA1, EXTRA2, EXTRA3, WARENR, BATCHLOTNR) " +
                "SELECT ProductionControllerNr, ProductionActionTypeNr, Parameters, WhereClause, SkipFactor, Repetition, CURRENT_TIMESTAMP, null AS ExtraValue1, null AS ExtraValue2, null AS ExtraValue3, null AS ExtraValue4, null AS ExtraValue5, null AS EXTRABOOLEAN1, null AS EXTRABOOLEAN2, null AS EXTRABOOLEAN3, null AS EXTRABOOLEAN4, null AS EXTRABOOLEAN5, null AS RECIPIENTKEYNR, null AS RECIPIENTAREA,'1' AS FORCECHECKREVISION, null AS ORDERPRODUCTIONNR, null AS PRODUCTIONLINENR, null AS EXTRA1, null AS EXTRA2, null AS EXTRA3, null AS WARENR, null AS BATCHLOTNR FROM tblProdLineRequest WHERE PRODUCTIONLINENR = " + prodLineNr + " AND PRODUCTIONACTIONTYPENR = " + type;
        executeUpdate(insertProdLineReq);
        FBConnectionClose();
    }

    void updateLaser(String orderNr, String editSSCC) {
        String type = "511";
		pressButton(orderNr,editSSCC,type);
    }

    void updateCaseLabeller(String orderNr, String editSSCC) {
        String type = "512";
        pressButton(orderNr,editSSCC,type);
    }

    void updateLogoPak(String orderNr, String editSSCC) {
        String type = "513";
        pressButton(orderNr,editSSCC,type);
    }

    void printToDesktop(String orderNr, String editSSCC) {
        String type = "514";
        pressButton(orderNr,editSSCC,type);
    }

    void partPallet(String orderNr, String editSSCC) {
		String type = "515";
		pressButton(orderNr,editSSCC,type);
    }

	int restore(String orderNr) {
		FBConnection();
		String restore = "UPDATE TBLORDERPRODUCTION SET STATUS = 6 WHERE ORDERPRODUCTIONNR = " + orderNr;
		executeUpdate(restore);
		FBConnectionClose();
		return 0;
	}

    void clearQueue(String orderNr, String editSSCC) {
        setSSCC(orderNr,editSSCC);
        list2 = getOrderData(Integer.parseInt(orderNr));
        FBConnection();
        int prodLineNr2 = getProductionLineNr(list2.get(0).getProductionLineNr());
        prodLineNr = Integer.toString(prodLineNr2);
        String insertProdLineReq = "INSERT INTO tblProductionRequest(ProductionLineNr,ProductionControllerNr,ProductionActionTypeNr, Parameters, WhereClause, SkipFactor, Repetition, FORCECHECKREVISION, RequestDateTime, ExtraValue1, ExtraValue2, ExtraValue3, ExtraValue4, ExtraValue5, EXTRABOOLEAN1, EXTRABOOLEAN2, EXTRABOOLEAN3, EXTRABOOLEAN4, EXTRABOOLEAN5, RECIPIENTKEYNR, RECIPIENTAREA, ORDERPRODUCTIONNR, EXTRA1, EXTRA2, EXTRA3, WARENR, BATCHLOTNR) " +
                "SELECT ProductionLineNr, ProductionControllerNr, ProductionActionTypeNr, Parameters, WhereClause, SkipFactor, Repetition, ForceCheckRevision, CURRENT_TIMESTAMP, '' AS ExtraValue1, '' AS ExtraValue2, '' AS ExtraValue3, '' AS ExtraValue4, '' AS ExtraValue5, '0' AS EXTRABOOLEAN1, '0' AS EXTRABOOLEAN2, '0' AS EXTRABOOLEAN3, '0' AS EXTRABOOLEAN4, '0' AS EXTRABOOLEAN5, '" + list2.get(0).getOrderProductionNr() + "' AS RECIPIENTKEYNR, 'ORDERPRODUCTION' AS RECIPIENTAREA, " + orderNr + " AS ORDERPRODUCTIONNR, '" + list2.get(0).getExtra1() + "' AS EXTRA1, '" + list2.get(0).getBatchLotNr().trim() + "' AS EXTRA2, ' ' AS EXTRA3, '" + list2.get(0).getWarenr().trim() + "', '" + list2.get(0).getBatchLotNr().trim() + "' FROM tblProdLineRequest WHERE PRODUCTIONLINENR = " + prodLineNr + " AND PRODUCTIONACTIONTYPENR = 511";
        executeUpdate(insertProdLineReq);
        FBConnectionClose();
    }

    int startOrder(String orderNr, String repack, String repackBatch, String repackDate, String SSCCYear, String caseCount, String editSSCC, String partPallet, String shopOrder) {
        list2 = getOrderData(Integer.parseInt(orderNr));
        String wareNr = list2.get(0).getWarenr();
        String serviceDescription = list2.get(0).getServicedescription();

        FBConnection();
        int prodLineNr2 = getProductionLineNr(list2.get(0).getProductionLineNr());
        prodLineNr = Integer.toString(prodLineNr2);
        boolean exists = checkOrderExists(orderNr);
        if (!exists) {
            setResourses(repack,repackBatch,repackDate,SSCCYear,caseCount,editSSCC,partPallet,shopOrder);
            String updateRepack = "UPDATE TBLORDERPRODUCTION set REPACK = '" + repack + "', REPACKBATCH = '" + repackBatch + "', REPACKDATE = '" + repackDate + "', SSCCYEAR = " + SSCCYear + " WHERE ORDERPRODUCTIONNR = " + list2.get(0).getOrderProductionNr();
            executeUpdate(updateRepack);
            String updateProdLine = "UPDATE TBLPRODUCTIONLINE set ACTIVEWARENR = '" + wareNr.trim() + "', ACTIVESERVICEDESCRIPTION = '" + serviceDescription.trim() + "', ACTIVEBATCHLOTNR = '" + list2.get(0).getBatchLotNr().trim() + "', ACTIVEORDERPRODUCTIONNR = " + list2.get(0).getOrderProductionNr().trim() + ", ACTIVELABELTEXT = '" + list2.get(0).getLabeltext().trim() + "', ACTIVELABELTEXT2 = '" + list2.get(0).getLabeltext2().trim() + "', ACTIVELABELTEXT3 = '" + list2.get(0).getLabeltext3().trim() + "' WHERE TBLPRODUCTIONLINE.PRODUCTIONLINENR = " + prodLineNr;
            executeUpdate(updateProdLine);
            System.out.println("updateProdLine done");
            String updateProdStatus = "UPDATE TBLORDERPRODUCTION SET STATUS = '7' WHERE ORDERPRODUCTIONNR = " + orderNr.trim() + " AND STATUS = '6'";
            executeUpdate(updateProdStatus);
            System.out.println("updateProdStatus done");
            String insertProdLineReq = "INSERT INTO tblProductionRequest(ProductionLineNr,ProductionControllerNr,ProductionActionTypeNr, Parameters, WhereClause, SkipFactor, Repetition, FORCECHECKREVISION, RequestDateTime, ExtraValue1, ExtraValue2, ExtraValue3, ExtraValue4, ExtraValue5, EXTRABOOLEAN1, EXTRABOOLEAN2, EXTRABOOLEAN3, EXTRABOOLEAN4, EXTRABOOLEAN5, RECIPIENTKEYNR, RECIPIENTAREA, ORDERPRODUCTIONNR, EXTRA1, EXTRA2, EXTRA3, WARENR, BATCHLOTNR) " +
                    "SELECT ProductionLineNr, ProductionControllerNr, ProductionActionTypeNr, Parameters, WhereClause, SkipFactor, Repetition, ForceCheckRevision, CURRENT_TIMESTAMP, '" + caseCount + "' AS ExtraValue1, '" + SSCCYear + "' AS ExtraValue2, '" + editSSCC + "' AS ExtraValue3, '" + partPallet + "' AS ExtraValue4, '" + repackBatch + "' AS ExtraValue5, '" + repack + "' AS EXTRABOOLEAN1, '0' AS EXTRABOOLEAN2, '0' AS EXTRABOOLEAN3, '0' AS EXTRABOOLEAN4, '0' AS EXTRABOOLEAN5, '" + list2.get(0).getOrderProductionNr() + "' AS RECIPIENTKEYNR, 'ORDERPRODUCTION' AS RECIPIENTAREA, " + orderNr + " AS ORDERPRODUCTIONNR, '" + list2.get(0).getExtra1() + "' AS EXTRA1, '" + list2.get(0).getBatchLotNr().trim() + "' AS EXTRA2, ' ' AS EXTRA3, '" + list2.get(0).getWarenr().trim() + "', '" + list2.get(0).getBatchLotNr().trim() + "' FROM tblProdLineRequest WHERE PRODUCTIONLINENR = " + prodLineNr + " AND PRODUCTIONACTIONTYPENR = 500";
            executeUpdate(insertProdLineReq);
            System.out.println("insertProdlineReq done");
            String updateActivityState = "UPDATE TBLORDERPRODUCTION SET ACTIVITYSTATE = 0 WHERE ORDERPRODUCTIONNR = " + orderNr;
            executeUpdate(updateActivityState);
            System.out.println("updateActivityState done");
            System.out.println("a running Production Order does exist so order has been started");
            FBConnectionClose();
            return 0; // a running Production Order does exist so order has been started
        } else {
            System.out.println("A running production order already exists");
            FBConnectionClose();
            return 102; // A running production order already exists
        }

    }

    private void setSSCC(String orderNr, String editSSCC) {

        FBConnection();
        list2 = getOrderData(Integer.parseInt(orderNr));
        int prodLineNr2 = getProductionLineNr(list2.get(0).getProductionLineNr());
        prodLineNr = Integer.toString(prodLineNr2);
        switch (prodLine) {
            case "Caledonian":
                String calEditSSCC = "INSERT INTO tblProductionRequest(ProductionLineNr,ProductionControllerNr,ProductionActionTypeNr, Parameters, WhereClause, SkipFactor, Repetition, FORCECHECKREVISION) VALUES( " + prodLineNr + ", 525, 500,'SET(RESOURCES.PALSSCC2," + editSSCC + ")', 'true', 0, 1, 1);";
                executeUpdate(calEditSSCC);
                break;
            case "FlexiLine":
                String flexiEditSSCC = "INSERT INTO tblProductionRequest(ProductionLineNr,ProductionControllerNr,ProductionActionTypeNr, Parameters, WhereClause, SkipFactor, Repetition, FORCECHECKREVISION) VALUES( " + prodLineNr + ", 527, 500, 'SET(RESOURCES.PALSSCC2," + editSSCC + ")', 'true', 0, 1, 1);";
                executeUpdate(flexiEditSSCC);
                break;
            case "Finishing":
                String finEditSSCC = "INSERT INTO tblProductionRequest(ProductionLineNr,ProductionControllerNr,ProductionActionTypeNr, Parameters, WhereClause, SkipFactor, Repetition, FORCECHECKREVISION) VALUES( " + prodLineNr + ", 526, 500, 'SET(RESOURCES.FIN_PALSSCC2," + editSSCC + ")', 'true', 0, 1, 1);";
                executeUpdate(finEditSSCC);
                break;
            case "Miniatures":
                String minEditSSCC = "INSERT INTO tblProductionRequest(ProductionLineNr,ProductionControllerNr,ProductionActionTypeNr, Parameters, WhereClause, SkipFactor, Repetition, FORCECHECKREVISION) VALUES( " + prodLineNr + ", 528, 500, 'SET(RESOURCES.MIN_PALSSCC2," + editSSCC + ")', 'true', 0, 1, 1);";
                executeUpdate(minEditSSCC);
                break;
            default:
                System.out.println("Unable to set SSCC resources");
                log.appLog("Unable to set SSCC resources\r\n");
                break;
        }
        FBConnectionClose();
    }
    public void setResourses(String repack, String repackBatch, String repackDate, String SSCCYear, String caseCount, String editSSCC, String partPallet, String shopOrder) {
        switch (prodLine) {
            case "Caledonian": {
				String calRepackResources =
						"INSERT INTO tblProductionRequest(ProductionLineNr,ProductionControllerNr,ProductionActionTypeNr, Parameters, WhereClause, SkipFactor, Repetition, FORCECHECKREVISION) VALUES( "
								+ prodLineNr + ", 525, 500, 'SET(RESOURCES.APP_REPACK," + repack + ")', 'true', 0, 1, 1);";
				executeUpdate(calRepackResources);
				String calRepackDateResources =
						"INSERT INTO tblProductionRequest(ProductionLineNr,ProductionControllerNr,ProductionActionTypeNr, Parameters, WhereClause, SkipFactor, Repetition, FORCECHECKREVISION) VALUES( "
								+ prodLineNr + ", 525, 500, 'SET(RESOURCES.APP_REPACKBATCH," + repackBatch + ")', 'true', 0, 1, 1);";
				executeUpdate(calRepackDateResources);
				String calRepackBatchResources =
						"INSERT INTO tblProductionRequest(ProductionLineNr,ProductionControllerNr,ProductionActionTypeNr, Parameters, WhereClause, SkipFactor, Repetition, FORCECHECKREVISION) VALUES( "
								+ prodLineNr + ", 525, 500, 'SET(RESOURCES.APP_REPACKDATE," + repackDate + ")', 'true', 0, 1, 1);";
				executeUpdate(calRepackBatchResources);
				String calRepackYearResources =
						"INSERT INTO tblProductionRequest(ProductionLineNr,ProductionControllerNr,ProductionActionTypeNr, Parameters, WhereClause, SkipFactor, Repetition, FORCECHECKREVISION) VALUES( "
								+ prodLineNr + ", 525, 500, 'SET(RESOURCES.APP_REPACKYEAR," + SSCCYear + ")', 'true', 0, 1, 1);";
				executeUpdate(calRepackYearResources);
				String calCaseCount =
						"INSERT INTO tblProductionRequest(ProductionLineNr,ProductionControllerNr,ProductionActionTypeNr, Parameters, WhereClause, SkipFactor, Repetition, FORCECHECKREVISION) VALUES( "
								+ prodLineNr + ", 525, 500, 'SET(RESOURCES.APP_CASECOUNT," + caseCount + ")', 'true', 0, 1, 1);";
				executeUpdate(calCaseCount);
				String calEditSSCC =
						"INSERT INTO tblProductionRequest(ProductionLineNr,ProductionControllerNr,ProductionActionTypeNr, Parameters, WhereClause, SkipFactor, Repetition, FORCECHECKREVISION) VALUES( "
								+ prodLineNr + ", 525, 500, 'SET(RESOURCES.APP_EDITSSCC," + editSSCC + ")', 'true', 0, 1, 1);";
				executeUpdate(calEditSSCC);
				String calPartPallet =
						"INSERT INTO tblProductionRequest(ProductionLineNr,ProductionControllerNr,ProductionActionTypeNr, Parameters, WhereClause, SkipFactor, Repetition, FORCECHECKREVISION) VALUES( "
								+ prodLineNr + ", 525, 500, 'SET(RESOURCES.APP_PARTPALLET," + partPallet + ")', 'true', 0, 1, 1);";
				executeUpdate(calPartPallet);
				String calShopOrder =
						"INSERT INTO tblProductionRequest(ProductionLineNr,ProductionControllerNr,ProductionActionTypeNr, Parameters, WhereClause, SkipFactor, Repetition, FORCECHECKREVISION) VALUES( "
								+ prodLineNr + ", 525, 500, 'SET(RESOURCES.APP_SHOPORDER," + shopOrder + ")', 'true', 0, 1, 1);";
				executeUpdate(calShopOrder);
				break;
			}
            case "FlexiLine": {
				String flexiRepackResources =
						"INSERT INTO tblProductionRequest(ProductionLineNr,ProductionControllerNr,ProductionActionTypeNr, Parameters, WhereClause, SkipFactor, Repetition, FORCECHECKREVISION) VALUES( "
								+ prodLineNr + ", 527, 500, 'SET(RESOURCES.APP_REPACK," + repack + ")', 'true', 0, 1, 1);";
				executeUpdate(flexiRepackResources);
				String flexiRepackDateResources =
						"INSERT INTO tblProductionRequest(ProductionLineNr,ProductionControllerNr,ProductionActionTypeNr, Parameters, WhereClause, SkipFactor, Repetition, FORCECHECKREVISION) VALUES( "
								+ prodLineNr + ", 527, 500, 'SET(RESOURCES.APP_REPACKBATCH," + repackBatch + ")', 'true', 0, 1, 1);";
				executeUpdate(flexiRepackDateResources);
				String flexiRepackBatchResources =
						"INSERT INTO tblProductionRequest(ProductionLineNr,ProductionControllerNr,ProductionActionTypeNr, Parameters, WhereClause, SkipFactor, Repetition, FORCECHECKREVISION) VALUES( "
								+ prodLineNr + ", 527, 500, 'SET(RESOURCES.APP_REPACKDATE," + repackDate + ")', 'true', 0, 1, 1);";
				executeUpdate(flexiRepackBatchResources);
				String flexiRepackYearResources =
						"INSERT INTO tblProductionRequest(ProductionLineNr,ProductionControllerNr,ProductionActionTypeNr, Parameters, WhereClause, SkipFactor, Repetition, FORCECHECKREVISION) VALUES( "
								+ prodLineNr + ", 527, 500, 'SET(RESOURCES.APP_REPACKYEAR," + SSCCYear + ")', 'true', 0, 1, 1);";
				executeUpdate(flexiRepackYearResources);
				String flexiCaseCount =
						"INSERT INTO tblProductionRequest(ProductionLineNr,ProductionControllerNr,ProductionActionTypeNr, Parameters, WhereClause, SkipFactor, Repetition, FORCECHECKREVISION) VALUES( "
								+ prodLineNr + ", 527, 500, 'SET(RESOURCES.APP_CASECOUNT," + caseCount + ")', 'true', 0, 1, 1);";
				executeUpdate(flexiCaseCount);
				String flexiEditSSCC =
						"INSERT INTO tblProductionRequest(ProductionLineNr,ProductionControllerNr,ProductionActionTypeNr, Parameters, WhereClause, SkipFactor, Repetition, FORCECHECKREVISION) VALUES( "
								+ prodLineNr + ", 527, 500, 'SET(RESOURCES.APP_EDITSSCC," + editSSCC + ")', 'true', 0, 1, 1);";
				executeUpdate(flexiEditSSCC);
				String flexiPartPallet =
						"INSERT INTO tblProductionRequest(ProductionLineNr,ProductionControllerNr,ProductionActionTypeNr, Parameters, WhereClause, SkipFactor, Repetition, FORCECHECKREVISION) VALUES( "
								+ prodLineNr + ", 527, 500, 'SET(RESOURCES.APP_PARTPALLET," + partPallet + ")', 'true', 0, 1, 1);";
				executeUpdate(flexiPartPallet);
				String flexiShopOrder =
						"INSERT INTO tblProductionRequest(ProductionLineNr,ProductionControllerNr,ProductionActionTypeNr, Parameters, WhereClause, SkipFactor, Repetition, FORCECHECKREVISION) VALUES( "
								+ prodLineNr + ", 527, 500, 'SET(RESOURCES.APP_SHOPORDER," + shopOrder + ")', 'true', 0, 1, 1);";
				executeUpdate(flexiShopOrder);
				break;
			}
            case "Finishing": {
				String finRepackResources =
						"INSERT INTO tblProductionRequest(ProductionLineNr,ProductionControllerNr,ProductionActionTypeNr, Parameters, WhereClause, SkipFactor, Repetition, FORCECHECKREVISION) VALUES( "
								+ prodLineNr + ", 526, 500, 'SET(RESOURCES.APP_REPACK," + repack + ")', 'true', 0, 1, 1);";
				executeUpdate(finRepackResources);
				String finRepackDateResources =
						"INSERT INTO tblProductionRequest(ProductionLineNr,ProductionControllerNr,ProductionActionTypeNr, Parameters, WhereClause, SkipFactor, Repetition, FORCECHECKREVISION) VALUES( "
								+ prodLineNr + ", 526, 500, 'SET(RESOURCES.APP_REPACKBATCH," + repackBatch + ")', 'true', 0, 1, 1);";
				executeUpdate(finRepackDateResources);
				String finRepackBatchResources =
						"INSERT INTO tblProductionRequest(ProductionLineNr,ProductionControllerNr,ProductionActionTypeNr, Parameters, WhereClause, SkipFactor, Repetition, FORCECHECKREVISION) VALUES( "
								+ prodLineNr + ", 526, 500, 'SET(RESOURCES.APP_REPACKDATE," + repackDate + ")', 'true', 0, 1, 1);";
				executeUpdate(finRepackBatchResources);
				String finRepackYearResources =
						"INSERT INTO tblProductionRequest(ProductionLineNr,ProductionControllerNr,ProductionActionTypeNr, Parameters, WhereClause, SkipFactor, Repetition, FORCECHECKREVISION) VALUES( "
								+ prodLineNr + ", 526, 500, 'SET(RESOURCES.APP_REPACKYEAR," + SSCCYear + ")', 'true', 0, 1, 1);";
				executeUpdate(finRepackYearResources);
				String finCaseCount =
						"INSERT INTO tblProductionRequest(ProductionLineNr,ProductionControllerNr,ProductionActionTypeNr, Parameters, WhereClause, SkipFactor, Repetition, FORCECHECKREVISION) VALUES( "
								+ prodLineNr + ", 526, 500, 'SET(RESOURCES.APP_CASECOUNT," + caseCount + ")', 'true', 0, 1, 1);";
				executeUpdate(finCaseCount);
				String finEditSSCC =
						"INSERT INTO tblProductionRequest(ProductionLineNr,ProductionControllerNr,ProductionActionTypeNr, Parameters, WhereClause, SkipFactor, Repetition, FORCECHECKREVISION) VALUES( "
								+ prodLineNr + ", 526, 500, 'SET(RESOURCES.APP_EDITSSCC," + editSSCC + ")', 'true', 0, 1, 1);";
				executeUpdate(finEditSSCC);
				String finPartPallet =
						"INSERT INTO tblProductionRequest(ProductionLineNr,ProductionControllerNr,ProductionActionTypeNr, Parameters, WhereClause, SkipFactor, Repetition, FORCECHECKREVISION) VALUES( "
								+ prodLineNr + ", 526, 500, 'SET(RESOURCES.APP_PARTPALLET," + partPallet + ")', 'true', 0, 1, 1);";
				executeUpdate(finPartPallet);
				String finShopOrder =
						"INSERT INTO tblProductionRequest(ProductionLineNr,ProductionControllerNr,ProductionActionTypeNr, Parameters, WhereClause, SkipFactor, Repetition, FORCECHECKREVISION) VALUES( "
								+ prodLineNr + ", 526, 500, 'SET(RESOURCES.APP_SHOPORDER," + shopOrder + ")', 'true', 0, 1, 1);";
				executeUpdate(finShopOrder);
				break;
			}
            case "Miniatures": {
				String minRepackResources =
						"INSERT INTO tblProductionRequest(ProductionLineNr,ProductionControllerNr,ProductionActionTypeNr, Parameters, WhereClause, SkipFactor, Repetition, FORCECHECKREVISION) VALUES( "
								+ prodLineNr + ", 528, 500, 'SET(RESOURCES.APP_REPACK," + repack + ")', 'true', 0, 1, 1);";
				executeUpdate(minRepackResources);
				String minRepackDateResources =
						"INSERT INTO tblProductionRequest(ProductionLineNr,ProductionControllerNr,ProductionActionTypeNr, Parameters, WhereClause, SkipFactor, Repetition, FORCECHECKREVISION) VALUES( "
								+ prodLineNr + ", 528, 500, 'SET(RESOURCES.APP_REPACKBATCH," + repackBatch + ")', 'true', 0, 1, 1);";
				executeUpdate(minRepackDateResources);
				String minRepackBatchResources =
						"INSERT INTO tblProductionRequest(ProductionLineNr,ProductionControllerNr,ProductionActionTypeNr, Parameters, WhereClause, SkipFactor, Repetition, FORCECHECKREVISION) VALUES( "
								+ prodLineNr + ", 528, 500, 'SET(RESOURCES.APP_REPACKDATE," + repackDate + ")', 'true', 0, 1, 1);";
				executeUpdate(minRepackBatchResources);
				String minRepackYearResources =
						"INSERT INTO tblProductionRequest(ProductionLineNr,ProductionControllerNr,ProductionActionTypeNr, Parameters, WhereClause, SkipFactor, Repetition, FORCECHECKREVISION) VALUES( "
								+ prodLineNr + ", 528, 500, 'SET(RESOURCES.APP_REPACKYEAR," + SSCCYear + ")', 'true', 0, 1, 1);";
				executeUpdate(minRepackYearResources);
				String minCaseCount =
						"INSERT INTO tblProductionRequest(ProductionLineNr,ProductionControllerNr,ProductionActionTypeNr, Parameters, WhereClause, SkipFactor, Repetition, FORCECHECKREVISION) VALUES( "
								+ prodLineNr + ", 528, 500, 'SET(RESOURCES.APP_CASECOUNT," + caseCount + ")', 'true', 0, 1, 1);";
				executeUpdate(minCaseCount);
				String minEditSSCC =
						"INSERT INTO tblProductionRequest(ProductionLineNr,ProductionControllerNr,ProductionActionTypeNr, Parameters, WhereClause, SkipFactor, Repetition, FORCECHECKREVISION) VALUES( "
								+ prodLineNr + ", 528, 500, 'SET(RESOURCES.APP_EDITSSCC," + editSSCC + ")', 'true', 0, 1, 1);";
				executeUpdate(minEditSSCC);
				String minPartPallet =
						"INSERT INTO tblProductionRequest(ProductionLineNr,ProductionControllerNr,ProductionActionTypeNr, Parameters, WhereClause, SkipFactor, Repetition, FORCECHECKREVISION) VALUES( "
								+ prodLineNr + ", 528, 500, 'SET(RESOURCES.APP_PARTPALLET," + partPallet + ")', 'true', 0, 1, 1);";
				executeUpdate(minPartPallet);
				String minShopOrder =
						"INSERT INTO tblProductionRequest(ProductionLineNr,ProductionControllerNr,ProductionActionTypeNr, Parameters, WhereClause, SkipFactor, Repetition, FORCECHECKREVISION) VALUES( "
								+ prodLineNr + ", 528, 500, 'SET(RESOURCES.APP_SHOPORDER," + shopOrder + ")', 'true', 0, 1, 1);";
				executeUpdate(minShopOrder);
				break;
			}
            default:
                System.out.println("Unable to set Repack resources");
                log.appLog("Unable to set Repack resources\r\n");
                break;
        }
    }

    private void incrementGenID() {
        String gen_id;
        String updateGen = "SELECT GEN_ID (GEN_ORDERPRODUCTIONSTARTED,1) FROM RDB$DATABASE";
        execute(updateGen);
        try {
            while (rs.next()) {
                gen_id = rs.getString("GEN_ID");
                System.out.println("Running Order exists count = " + gen_id);
            }
        } catch(SQLException e) {
            System.out.println("Failed to increment GEN_ID");
            e.printStackTrace();

        }
    }

    private boolean checkOrderRunning(String orderNr) {
        String count = "99";
        prodLineNr = mainController.getProductionLineNr(prodLine);
        String countSQL = "SELECT COUNT (STATUS) FROM TBLORDERPRODUCTION WHERE PRODUCTIONLINENR = " + prodLineNr + " AND STATUS = 7 AND ORDERPRODUCTIONNR = " + orderNr;
        execute(countSQL);
        try {
            while (rs.next()) {
                count = rs.getString("COUNT");
                System.out.println("Running Order exists count = " + count);
            }
        } catch(SQLException e) {
            System.out.println("Failed to get order exists count");
            e.printStackTrace();
        }
        if (count.equals("0")) {
            System.out.println("This order is NOT running");
            return false;
        } else {
            System.out.println("This order is running");
            return true;
        }
    }
    private boolean checkOrderExists(String orderNr) {
        String count = "99";
        String prodLineNr = mainController.getProductionLineNr(prodLine);
        String countSQL = "SELECT COUNT(PRODUCTIONLINENR) FROM TBLORDERPRODUCTION WHERE PRODUCTIONLINENR = " + prodLineNr + " AND STATUS = 7";
        execute(countSQL);
        try {
            while (rs.next()) {
                count = rs.getString("COUNT");
                System.out.println("Order exists count = " + count);
            }
        } catch(SQLException e) {
            System.out.println("Failed to get order exists count");
            e.printStackTrace();
            log.appLog(e.toString()+"\r\n");
            log.appLog("Failed to get order exists count\r\n");
        }
        if (count.equals("0")) {
            return false;
        } else {
            System.out.println("An order is already running");
            return true;
        }
    }

    private void execute( String sqlStatement) {
        try{
            pstmt = conn.prepareStatement(sqlStatement);
            rs = pstmt.executeQuery();
        } catch (SQLException e) {
            System.out.println("Failed to prepare statement");
            e.printStackTrace();
        }
    }

    private void executeUpdate( String sqlStatement) {
        try{
            pstmt = conn.prepareStatement(sqlStatement);
            pstmt.executeUpdate();
        } catch (SQLException e) {
            System.out.println("Failed to executeUpdate");
            e.printStackTrace();
            log.appLog(e.toString()+"\r\n");
            log.appLog("Failed to executeUpdate\r\n");
        }
    }

    String FBgetSSCC() {
        String result ="";
        FBConnection();

        if (prodLine.equals(caledonian)) {
            System.out.println("ProductionLine = " + mainController.getProductionLine().trim());
            System.out.println("Getting CAL_PALLET_SSCC ...");
            execute(calSSCC);
            try {
                while (rs.next()) {
                    result = rs.getString("CAL_PALLET_SSCC");
                    System.out.println("CAL_PALLET_SSCC " + result);
                }
            } catch(SQLException e) {
                System.out.println("Failed to get CAL_PALLET_SSCC");
                e.printStackTrace();
                log.appLog(e.toString()+"\r\n");
                log.appLog("Failed to get CAL_PALLET_SSCC\r\n");
            }
        } else if (prodLine.equals(flexiLine)) {
            System.out.println("Getting FLEX_PALLET_SSCC ...");
            System.out.println("ProductionLine = " + mainController.getProductionLine());
            execute(flexSSCC);
            try {
                while (rs.next()) {
                    result = rs.getString("FLEX_PALLET_SSCC");
                    System.out.println("FLEX_PALLET_SSCC " + result);
                }
            } catch(SQLException e) {
                System.out.println("Failed to get FLEX_PALLET_SSCC");
                e.printStackTrace();
                log.appLog(e.toString()+"\r\n");
                log.appLog("Failed to get FLEX_PALLET_SSCC\r\n");
            }
        } else if (prodLine.equals(finishing)) {
            System.out.println("Getting FIN_PALLET_SSCC ...");
            System.out.println("ProductionLine = " + mainController.getProductionLine());
            execute(finSSCC);
            try {
                while (rs.next()) {
                    result = rs.getString("FIN_PALLET_SSCC");
                    System.out.println("FIN_PALLET_SSCC " + result);
                }
            } catch(SQLException e) {
                System.out.println("Failed to get FIN_PALLET_SSCC");
                e.printStackTrace();
                log.appLog(e.toString()+"\r\n");
                log.appLog("Failed to get FIN_PALLET_SSCC\r\n");
            }
        } else if (prodLine.equals(miniatures)) {
            System.out.println("Getting MIN_PALLET_SSCC ...");
            System.out.println("ProductionLine = " + mainController.getProductionLine());
            execute(minSSCC);
            try {
                while (rs.next()) {
                    result = rs.getString("MIN_PALLET_SSCC");
                    System.out.println("MIN_PALLET_SSCC " + result);
                }
            } catch(SQLException e) {
                System.out.println("Failed to get MIN_PALLET_SSCC");
                e.printStackTrace();
                log.appLog(e.toString()+"\r\n");
                log.appLog("Failed to get MIN_PALLET_SSCC\r\n");
            }
        } else {
            System.out.println("Getting CAL_PALLET_SSCC ...");
            System.out.println("ProductionLine = " + mainController.getProductionLine());
            execute(calSSCC);
            try {
                while (rs.next()) {
                    result = rs.getString("CAL_PALLET_SSCC");
                    System.out.println("CAL_PALLET_SSCC  paul" + result);
                }
            } catch(SQLException e) {
                System.out.println("Failed to get CAL_PALLET_SSCC");
                e.printStackTrace();
                log.appLog(e.toString()+"\r\n");
                log.appLog("Failed to get CAL_PALLET_SSCC\r\n");
            }
        }
        FBConnectionClose();
        return ("000000" + result).substring(result.length());
    }

    ObservableList<String> FBWarenr(String sqlStatement){
        FBConnection();
        System.out.println("Creating product statement...");
        try{
            pstmt = conn.prepareStatement(sqlStatement,ResultSet.TYPE_SCROLL_INSENSITIVE,ResultSet.CONCUR_UPDATABLE);
            rs = pstmt.executeQuery();
        } catch (SQLException e) {
            System.out.println("Failed to prepare product statement");
            e.printStackTrace();
            log.appLog(e.toString()+"\r\n");
            log.appLog("Failed to prepare product statement\r\n");
        }
        System.out.println("Creating product list");

        String warenr;
        try{
            while(rs.next()) {
                warenr = rs.getString("warenr");
                wareList.add(warenr);
            }
        } catch (SQLException e){
            System.out.println("Failed to get warenr resultset");
            e.printStackTrace();
            log.appLog(e.toString()+"\r\n");
            log.appLog("Failed to get warenr resultset\r\n");
        }
        FBConnectionClose();
        return wareList;
    }

    int createNewOrder(String warenr, String rotation, String shopOrder) {
        int prodLineNr = getProductionLineNr(prodLine);
        String SAPcode = getSAPcode(warenr);
        int autoOrderNr = getAutoOrderNr();
        String currentTime = new DateTime().getCurrentTimeStamp("dd.MM.yyyy,HH:mm:ss.SSS");
        int startStatus = 6;
        int PlannedUnits = 0;
        int activeState;
        activeState = 0;
        FBConnection();
        String sqlStatement = "INSERT INTO TBLORDERPRODUCTION(ORDERPRODUCTIONNR, STARTDATETIME, STATUS, PRODUCTIONLINENR, WARENR," +
                "SERVICEDESCRIPTION, STOPDATETIME, PLANNEDUNITS, BATCHLOTNR, ESXORDERPRODUCTIONNR,ACTIVITYSTATE, EXTRA1)" +
                " values (" + autoOrderNr + ", CAST('"+ currentTime + "' AS TIMESTAMP), " +  startStatus + ", " + prodLineNr + ", '" + warenr + "', '" +
                SAPcode + "', CAST('" + currentTime + "' AS TIMESTAMP), " + PlannedUnits + ", '" + rotation + "', '" + autoOrderNr + "', " + activeState + ", '" + shopOrder + "');";
        executeUpdate(sqlStatement);
        String sqlStatement2 = "INSERT INTO TBLBATCHLOT(BATCHLOTNR, WARENR, PRODUCTIONLINENR, CREATEDATETIME, ESXBATCHLOTNR,BESTBEFORE) " +
                " VALUES(" + autoOrderNr + ", '" + warenr + "', " + prodLineNr + ", CAST('" + currentTime + "' AS TIMESTAMP), " + autoOrderNr + ", CAST('" +  currentTime + "' AS TIMESTAMP));";
            executeUpdate(sqlStatement2);
        FBConnectionClose();
        return autoOrderNr;
    }

    public void deleteOrder(int orderNr) {
        FBConnection();
        String sqlStatement = " DELETE FROM TBLORDERPRODUCTION WHERE ORDERPRODUCTIONNR = " + orderNr + ";";
        executeUpdate(sqlStatement);
        FBConnectionClose();
    }

    private int getProductionLineNr(String line) {
        int result = 101; // ERROR CODE 101 - Line not recognised
        FBConnection();
        String sqlStatement = "SELECT PRODUCTIONLINENR FROM TBLPRODUCTIONLINE WHERE ITEMNAME = '" + line + "'";
        execute(sqlStatement);
        try {
            while (rs.next()) {
                result = rs.getInt("PRODUCTIONLINENR");
            }
        } catch(SQLException e) {
            System.out.println("Failed to getProductionLineNr");
            e.printStackTrace();
            log.appLog(e.toString()+"\r\n");
            log.appLog("Failed to getProductionLineNr\r\n");
        }
        return result;
    }

    private int getAutoOrderNr() {
        int result = 102; // ERROR CODE 102 - Failed to generate auto-number
        FBConnection();
        String sqlStatement = "SELECT  Gen_ID(GEN_ORDERPRODUCTIONNR, 1) FROM RDB$DATABASE";
        execute(sqlStatement);
        try {
            while (rs.next()) {
                result = rs.getInt("GEN_ID");
            }
        } catch(SQLException e) {
            System.out.println("Failed to getAutoOrderNr");
            e.printStackTrace();
            log.appLog(e.toString()+"\r\n");
            log.appLog("Failed to getAutoOrderNr\r\n");
        }
        return result;
    }

    private String getSAPcode(String warenr) {
        System.out.println("FirebirdConnect.getSAPcode");
        String result = "103"; // ERROR CODE 103 - Failed to get SAP Code
        FBConnection();
        String sqlStatement = "SELECT SERVICEDESCRIPTION FROM TBLWARE WHERE WARENR = '" + warenr + "'";
        execute(sqlStatement);
        try {
            while (rs.next()) {
                result = rs.getString("SERVICEDESCRIPTION");
            }
        } catch(SQLException e) {
            System.out.println("Failed to getSAPcode");
            e.printStackTrace();
            log.appLog(e.toString()+"\r\n");
            log.appLog("Failed to getSAPcode\r\n");
        }
        return result;
    }

    ObservableList<OpenOrder> getOrderData(int orderNr) {
        System.out.println("FirebirdConnect.getOrderData");
        OrderSQL = "SELECT a.WARENR, a. ORDERPRODUCTIONNR, a.EXTRA1, a.BATCHLOTNR, a.REPACK, a.REPACKDATE, a.REPACKBATCH, a.SSCCYEAR, a.STATUS, a.PRODUCTIONLINENR, a.STARTDATETIME, b.LABELTEXT, b.LABELTEXT2, b.SERVICEDESCRIPTION, b.BARCODEC, b.BARCODED, b.GRAMWEIGHTPERDPACK, b.LABELTEXT3, b.BARCODECLABEL, b.BARCODEDLABEL, b.BARCODETLABEL, b.DTOCRATIO, b.DTOLAYERRATIO, b.TTODRATIO FROM TBLORDERPRODUCTION a, TBLWARE b WHERE a.WARENR = b.WARENR AND ORDERPRODUCTIONNR = " + orderNr;

        FBConnection();
        System.out.println("Creating OrderSQL statement...");
        try {
            pstmt = conn.prepareStatement(OrderSQL, ResultSet.TYPE_SCROLL_INSENSITIVE, ResultSet.CONCUR_UPDATABLE);
            rs = pstmt.executeQuery();
        } catch (SQLException e) {
            System.out.println("Failed to prepare OrderSQL statement");
            e.printStackTrace();
            log.appLog(e.toString()+"\r\n");
            log.appLog("Failed to prepare OrderSQL statement\r\n");
        }
        System.out.println("Creating list");
        ObservableList<OpenOrder> list = FXCollections.observableArrayList();
        OpenOrder openOrder;
        try {
            while (rs.next()) {
                String status;
                int sta = rs.getInt("status");
                switch (sta) {
                    case 6:
                        status = "READY";
                        break;
                    case 7:
                        status = "STARTED";
                        break;
                    case 8:
                        status = "DONE";
                        break;
                    default:
                        status = "UNKNOWN";
                        break;
                }
                String line;
                int lin = rs.getInt("productionLineNr");
                switch (lin) {
                    case 15:
                        line = "CALEDONIAN";
                        break;
                    case 16:
                        line = "FINISHING";
                        break;
                    case 17:
                        line = "FLEXILINE";
                        break;
                    case 18:
                        line = "MINIATURES";
                        break;
                    default:
                        line = "UNKNOWN";
                        break;
                }

                String repack = rs.getString("repack");
                if (rs.wasNull()) {repack = "0";}

                String repackBatch = rs.getString("repackBatch");
                if (rs.wasNull()) { repackBatch = "0";}

                String repackDate = rs.getString("repackDate");
                if(rs.wasNull()) { repackDate = null;}


                String repackYear = rs.getString("SSCCYear");
                if (rs.wasNull()) { repackYear = "0";}

                openOrder = new OpenOrder(rs.getString("warenr"), String.valueOf(rs.getInt("orderProductionNr")), rs.getString("extra1"), rs.getString("batchlotnr"), repack, repackDate, repackBatch, repackYear, status, line, rs.getString("startDateTime"),rs.getString("labelText"),rs.getString("labelText2"),rs.getString("serviceDescription"),rs.getString("barcodeC"),rs.getString("barcodeD"),String.valueOf(rs.getInt("gramWeightPerDPack")),rs.getString("labelText3"),rs.getString("barcodeCLabel"),rs.getString("barcodeDLabel"),rs.getString("barcodeTLabel"),String.valueOf(rs.getInt("dtocratio")),String.valueOf(rs.getInt("dtolayerratio")),String.valueOf(rs.getInt("ttodratio")));
                list.add(openOrder);
            }
        } catch (SQLException e) {
            System.out.println("Failed to get openOrders resultset");
            e.printStackTrace();
            log.appLog(e.toString()+"\r\n");
            log.appLog("Failed to get openOrders resultset\r\n");
        }
        FBConnectionClose();
        return list;
    }
}
