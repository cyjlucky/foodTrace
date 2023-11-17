package com.foodtrace.utils.traceNumber;

import java.io.IOException;


/**
 * 调用 py 脚本
 */
public class GetPy {

    public static void proc(String[] args) throws IOException, InterruptedException {
        Process proc = Runtime.getRuntime().exec(args);
//        BufferedReader reader = new BufferedReader(new InputStreamReader(proc.getInputStream()));
//        String line;
//        while ((line = reader.readLine()) != null) {
//        }
//        System.out.println();
        proc.waitFor();
    }

}
