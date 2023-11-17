package com.foodtrace.controller;

import com.foodtrace.result.R.R;
import com.foodtrace.service.food.FoodService;
import com.foodtrace.vo.bo.FoodBO;
import lombok.Data;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.format.annotation.DateTimeFormat;
import org.springframework.web.bind.annotation.*;


@RestController
@RequestMapping("/food")
public class FoodController {

    @Autowired
    private FoodService foodService;

    @PostMapping("/newFood")
    @ResponseBody
    public R newFood(@RequestBody FoodBO food, @RequestHeader(required = false) String token) throws InterruptedException {
        return foodService.newFood(food, token);
    }

    @PostMapping("/addTraceInfoByDistributor")
    @ResponseBody
    public R addTraceInfoByDistributor(@RequestBody FoodBO food, @RequestHeader(required = false) String token){
        return foodService.addTraceInfoByDistributor(food, token);
    }

    @PostMapping("/addTraceInfoByRetailer")
    @ResponseBody
    public R addTraceInfoByRetailer(@RequestBody FoodBO food, @RequestHeader(required = false) String token){
        return foodService.addTraceInfoByRetailer(food, token);
    }

    @GetMapping("/getTraceInfo")
    @ResponseBody
    public R getTraceInfo(String traceNumber, @RequestHeader(required = false) String token){
        return foodService.getTraceInfo(traceNumber, token);
    }

    @GetMapping("/getFoodByTime")
    @ResponseBody
    public R getFoodByTime(String minTime,String maxTime, int current, int size, @RequestHeader(required = false) String token){
        return foodService.getFoodByTime(minTime, maxTime, current, size, token);
    }

    @GetMapping("/getAllFoodByTime")
    @ResponseBody
    public R getAllFoodByTime(String minTime,String maxTime, @RequestHeader(required = false) String token){
        return foodService.getAllFoodByTime(minTime, maxTime, token);
    }

    @GetMapping("/getAllFoodByTimePC")
    @ResponseBody
    public R getAllFoodByTimePC(int type, @RequestHeader(required = false) String token){
        return foodService.getAllFoodByTimePC(type, token);
    }

    @GetMapping("/getFoodByTraceNumber")
    @ResponseBody
    public R getFoodByTraceNumber(String traceNumber, @RequestHeader(required = false) String token){
        return foodService.getFoodByTraceNumber(traceNumber, token);
    }

    @GetMapping("/getFoodByBatches")
    @ResponseBody
    public R getFoodByBatches(String batches, @RequestHeader(required = false) String token){
        return foodService.getFoodByBatches(batches, token);
    }

    @GetMapping("/getFoodByBatchesPAGE")
    @ResponseBody
    public R getFoodByBatchesPAGE(String batches, int current, int size, @RequestHeader(required = false) String token){
        return foodService.getFoodByBatchesPAGE(batches, current, size, token);
    }

}
