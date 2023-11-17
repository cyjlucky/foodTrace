package com.foodtrace.vo.bo;

import com.fasterxml.jackson.annotation.JsonFormat;
import lombok.Data;
import org.springframework.format.annotation.DateTimeFormat;

@Data
public class FoodBO {
    private int id;

    private String traceNumber;

    private String foodName;

    private int expiration;

    private String origin;

    private String timestamp;

    private int quality;

    private String specification;

    private int status;

    private String batches;

    private int count;

    private String operator;

}
