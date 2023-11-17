package com.foodtrace.utils.traceNumber;

import java.util.UUID;

/**
 * 生成溯源码
 */
public class CreateTraceNumber {

    public static String create(String signature) {
        String traceNumber = UUID.randomUUID().toString().replace("-", "")
                .substring(0, 8)
                + "-"
                + signature.toString()
                + "-"
                + String.format("%04d", (int) (System.currentTimeMillis() / 1000));
        return traceNumber;
    }

}
