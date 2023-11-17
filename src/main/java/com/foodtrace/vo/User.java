package com.foodtrace.vo;

import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableField;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import lombok.Data;

import java.util.List;

@Data
@TableName("t_user")
public class User {
    @TableId(type = IdType.AUTO)
    private int id;

    private String userName;

    private String password;

    private String companyName;

    private String corporate;

    private String location;

    private String phoneNumber;

    private String license;

    private String address;

    private String publicKey;

    private int state;

    private int userType;

    @TableField(exist = false)
    private List<Food> foods;

}
