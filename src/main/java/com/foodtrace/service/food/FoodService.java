package com.foodtrace.service.food;

import com.baomidou.mybatisplus.extension.service.IService;
import com.foodtrace.result.R.R;
import com.foodtrace.vo.Food;
import com.foodtrace.vo.bo.FoodBO;
import lombok.Data;
import org.springframework.web.bind.annotation.RequestHeader;

public interface FoodService extends IService<Food> {
    R newFood(FoodBO food, String token) throws InterruptedException;
    R addTraceInfoByDistributor(FoodBO food, String token);
    R addTraceInfoByRetailer(FoodBO food, String token);
    R getFoodInfo(String traceNumber, String token);
    R getTraceInfo(String traceNumber, String token);
    R getFoodByTime(String minTime, String maxTime, int current, int size, String token);
    R getAllFoodByTime(String minTime, String maxTime, String token);
    R getAllFoodByTimePC(int type, String token);
    R getFoodByTraceNumber(String traceNumber, String token);
    R getFoodByBatches(String batches, String token);
    R getFoodByBatchesPAGE(String batches, int current, int size, String token);

    R removeFoodList();

}
