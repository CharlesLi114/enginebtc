package com.csc108.enginebtc.utils;

import org.apache.commons.lang3.StringUtils;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;

/**
 * Created by LI JT on 2019/9/11.
 * Description:
 */
public class Utils {

    public static String getSymbol(String stockId) {
        return StringUtils.substringBefore(stockId, ".");
    }

    public static String getExchange(String stockId) {
        return StringUtils.substringAfter(stockId, ".");
    }

    /**
     * Exec batch using standard java exec in a blocking way. Command is formed like
     * String command = "D:\\projects\\J\\enginebtc\\src\\main\\python\\sync_for_enginebtc\\py\\exec_sync.bat" + " " + 20191113;
     * @param batchFile     Batch file to execute.
     * @param args          If multiple inputs, should be merged into one string as args before exec.
     * @return              Execution return value.
     * @throws IOException if execution fails
     */
    public static int execBatch(String batchFile, String args) throws IOException {
        String command = batchFile + " " + args;
        Process process = Runtime.getRuntime().exec(new String[] {"cmd.exe","/c",command});

        BufferedReader stdInput = new BufferedReader(new
                InputStreamReader(process.getInputStream()));

        BufferedReader stdError = new BufferedReader(new
                InputStreamReader(process.getErrorStream()));

        // Read the output from the command
        System.out.println("Standard output of the command:\n");
        String s;
        while ((s = stdInput.readLine()) != null) {
            System.out.println(s);
        }

        System.out.println("Error output of the command:\n");
        while ((s = stdError.readLine()) != null) {
            System.out.println(s);
        }

        //        process.waitFor();
        return process.exitValue();
    }

    /**
     * Calls batch or exe file to run in a non-blocking way. This program will terminate when targets are called up.
     * @param file
     * @param args
     * @throws IOException
     */
    public static void startJob(String file, String args) throws IOException {
        String cmd;
        if (args == null) {
            cmd = file;
        } else {
            cmd = file + " " + args;
        }
        String[] command = new String[] {"cmd.exe", "/c", "start", cmd};
        Process p = Runtime.getRuntime().exec(command);
    }

}
