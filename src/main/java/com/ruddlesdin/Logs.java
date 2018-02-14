package com.ruddlesdin;

/**
 * Created by p_ruddlesdin on 21/03/2017.
 */
import java.io.BufferedWriter;
import java.io.File;
import java.io.FileWriter;
import java.io.IOException;

class Logs {
    private static final String logPath = "C:\\ProgramData\\QuickDesignUI\\";
    private static final String userLogPath = "C:\\ProgramData\\QuickDesignUI\\userLog";
    private static final String appLogPath = "C:\\ProgramData\\QuickDesignUI\\appLog";

    private BufferedWriter bw = null;
    private FileWriter fw = null;

    Logs(){
        logPath();
    }

    void userLog(String message){
        try {
            File file = new File(userLogPath + "_" + weekNo() + ".log");

            // if file doesn't exist, then create it;
            if (!file.exists())
            {
                file.createNewFile();
            }

            //true = append file
            fw = new FileWriter(file.getAbsoluteFile(),true);
            bw = new BufferedWriter(fw);
            bw.write(dateTime() + message + "\n\r");
            bw.flush();
        } catch (IOException e) {
            System.out.println("Problem writing to userLog file");
            e.printStackTrace();
        } finally {
            try {
                if (bw != null)
                    bw.close();
                if (fw != null)
                    fw.close();
            } catch (IOException ex) {
                System.out.println("Problem closing userFileWriter");
                ex.printStackTrace();
            }
        }
    }

    void appLog(String message){
        try {
            File file = new File(appLogPath + "_" + weekNo() + ".log");

            // if file doesn't exist, then create it;
            if (!file.exists())
            {
                file.createNewFile();
            }

            //true = append file
            fw = new FileWriter(file.getAbsoluteFile(),true);
            bw = new BufferedWriter(fw);
            bw.write(dateTime() + message + "\n\r");
            bw.flush();
        } catch (IOException e) {
            System.out.println("Problem writing to appLog file");
            e.printStackTrace();
        } finally {
            try {
                if (bw != null)
                    bw.close();
                if (fw != null)
                    fw.close();
            } catch (IOException ex) {
                System.out.println("Problem closing appFileWriter");
                ex.printStackTrace();
            }
        }
    }

    private void logPath() {
        File file = new File(logPath);
        if(!file.exists() && !file.isDirectory()) {
            boolean successful = file.mkdir();
            if(successful) {
                System.out.println("Log file path created successfully");
                appLog("Log file path created successfully\r\n");
            } else {
                System.out.println("Log file path already exists");
                appLog("Log file path already exists\r\n");
            }
        }
    }
    private String dateTime(){
        DateTime dt = new DateTime();
        return dt.getCurrentTimeStamp("yyyy.MM.dd HH:mm:ss.SSS ");
    }

    private String weekNo() {
        DateTime dt = new DateTime();
        return dt.getCurrentTimeStamp("wwyy");
    }


}
