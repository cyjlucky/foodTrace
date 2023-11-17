package com.foodtrace;

import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.foodtrace.config.propertiesConfig.PatternProperties;
import com.foodtrace.mapper.FoodMapper;
import com.foodtrace.mapper.UserAndFoodMapper;
import com.foodtrace.vo.Food;
import com.foodtrace.vo.UserAndFood;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.test.context.SpringBootTest;

import java.util.List;


@SpringBootTest
public class tessss {


    @Value("${pattern.myWeBASEAndPort}")
    private String myIP;
    @Value("${pattern.API.privateKey}")
    private String privateKey;

    @Test
    public void getURL(){
        System.out.println(myIP + privateKey);
    }


    @Autowired
    private PatternProperties properties;

    @Test
    public void conf(){
        System.out.println(properties.getMyWeBASEAndPort());
    }

    @Autowired
    private FoodMapper foodMapper;

    @Autowired
    private UserAndFoodMapper userAndFoodMapper;

    @Test
    public void page(){
        com.baomidou.mybatisplus.extension.plugins.pagination.Page<UserAndFood> page = new com.baomidou.mybatisplus.extension.plugins.pagination.Page<>(1,5);
        QueryWrapper<UserAndFood> userAndFoodQueryWrapper = new QueryWrapper<>();
        Page<UserAndFood> userAndFoodPage = userAndFoodMapper.selectPage(page, userAndFoodQueryWrapper);
        System.out.println(userAndFoodPage.getRecords().getClass());
    }
}
