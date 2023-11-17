package com.foodtrace.vo;

import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableField;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import com.fasterxml.jackson.annotation.JsonFormat;
import lombok.Data;
import org.springframework.format.annotation.DateTimeFormat;


@Data
@TableName("t_user_foodinfo")
public class UserAndFood {

    @TableId(type = IdType.AUTO)
    private int id;

    private int userId;

    private String traceNumber;

    private String operator;

    private String origin;

    private String timestamp;
}
