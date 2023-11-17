package com.foodtrace.service.food;

import cn.hutool.json.JSONArray;
import cn.hutool.json.JSONObject;
import cn.hutool.json.JSONUtil;
import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import com.baomidou.mybatisplus.core.conditions.update.UpdateWrapper;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.foodtrace.mapper.FoodMapper;
import com.foodtrace.mapper.UserAndFoodMapper;
import com.foodtrace.mapper.UserMapper;
import com.foodtrace.result.R.R;
import com.foodtrace.service.userAndFood.UserAndFoodService;
import com.foodtrace.utils.thread.ThreadService;
import com.foodtrace.utils.traceNumber.CreateTraceNumber;
import com.foodtrace.utils.RedisUtils;
import com.foodtrace.utils.traceNumber.GetPy;
import com.foodtrace.utils.webaseURL.WeBASEUtils;
import com.foodtrace.vo.Food;
import com.foodtrace.vo.User;
import com.foodtrace.vo.UserAndFood;
import com.foodtrace.vo.bo.FoodBO;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.io.IOException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

@Service
public class FoodServiceImpl extends ServiceImpl<FoodMapper, Food> implements FoodService {

    @Autowired
    private RedisUtils redisUtils;

    @Autowired
    private UserMapper userMapper;

    @Autowired
    private FoodMapper foodMapper;

    @Autowired
    private UserAndFoodMapper userAndFoodMapper;

    @Autowired
    private UserAndFoodService userAndFoodService;

    @Autowired
    private ThreadService threadService;

    @Autowired
    private WeBASEUtils weBASEUtils;

    @Override
    public R newFood(FoodBO foodBO, String token) throws InterruptedException {
        if (token == null) {
            return R.error(402, "用户未登录!");
        }
        if (redisUtils.get(token) == null) {
            return R.error(502, "身份令牌已过期!");
        }

        for (int i = 0; i < foodBO.getCount(); i++) {
            Thread.sleep(1); // 休眠一毫秒
            // 生成 溯源码 和 条形码
            String traceNumber = CreateTraceNumber.create((redisUtils.get(token)));
            Runnable task = () -> {
                // 异步逻辑
                String[] args = new String[]{"python", "E:\\学习资料\\毕业设计-线上食品超市平台\\食品溯源系统\\foodTrace\\src\\main\\resources\\python\\generate_barcodes.py",  // 生成 条形码 的脚本 所在位置
                        traceNumber    // 溯源码
                };
                try {
                    GetPy.proc(args);
                } catch (InterruptedException | IOException e) {
                    throw new RuntimeException(e);
                }
            };
            // 异步 调用 Python 脚本
            threadService.executeAsyncTask(task);

            // 处理两个表的实体类
            Food food = new Food();
            UserAndFood userAndFood = new UserAndFood();

            food.setTraceNumber(traceNumber);
            food.setFoodName(foodBO.getFoodName());
            food.setExpiration(foodBO.getExpiration());
            food.setQuality(foodBO.getQuality());
            food.setSpecification(foodBO.getSpecification());
            food.setStatus(1);
            food.setBatches(foodBO.getBatches());
            food.setCount(1);

            userAndFood.setOrigin(foodBO.getOrigin());
            userAndFood.setTimestamp(foodBO.getTimestamp());
            userAndFood.setOperator(foodBO.getOperator());

            // 查询用户的信息
            QueryWrapper<User> wrapper = new QueryWrapper<>();
            wrapper.eq("user_name", (redisUtils.get(token)));
            User user = userMapper.selectOne(wrapper);

            List<java.io.Serializable> param = new ArrayList<java.io.Serializable>();
            param.add(traceNumber);
            param.add(food.getFoodName());
            param.add(food.getExpiration());
            param.add(userAndFood.getOrigin());
            param.add(userAndFood.getTimestamp());
            param.add(food.getQuality());
            param.add(food.getBatches());
            param.add(userAndFood.getOperator());
            param.add(user.getAddress());

            // 食品信息上链
            String newFood = weBASEUtils.funcPost(user.getUserName(), "newFood", param);
            JSONObject res = JSONUtil.parseObj(newFood);
            if (!res.get("message").equals("Success")) {
                return R.error(407, res.get("message").toString());
            }
            userAndFood.setTxHash((String) res.get("transactionHash"));
            userAndFood.setBlockNumber((String) res.get("blockNumber"));
            // 食品信息插入数据库
            foodMapper.insert(food);

            // 设置外键, 这里应该处理第三个表
            userAndFood.setUserId(user.getId());
            userAndFood.setTraceNumber(traceNumber);
            userAndFoodService.setUidAndFid(userAndFood);
        }

        return R.ok();
    }

    @Override
    public R addTraceInfoByDistributor(FoodBO foodBO, String token) {
        if (token == null) {
            return R.error(402, "用户未登录!");
        }
        if (redisUtils.get(token) == null) {
            return R.error(502, "身份令牌已过期!");
        }

        // 处理两个表的实体类
        Food food = new Food();
        UserAndFood userAndFood = new UserAndFood();

        food.setTraceNumber(foodBO.getTraceNumber());
        food.setStatus(2);
        food.setQuality(foodBO.getQuality());

        userAndFood.setOrigin(foodBO.getOrigin());
        userAndFood.setTimestamp(foodBO.getTimestamp());
        userAndFood.setOperator(foodBO.getOperator());

        // 查询用户的信息
        QueryWrapper<User> userQueryWrapper = new QueryWrapper<>();
        userQueryWrapper.eq("user_name", (redisUtils.get(token)));
        User user = userMapper.selectOne(userQueryWrapper);

        List<java.io.Serializable> param = new ArrayList<java.io.Serializable>();
        param.add(food.getTraceNumber());
        param.add(userAndFood.getOrigin());
        param.add(userAndFood.getTimestamp());
        param.add(food.getQuality());
        param.add(userAndFood.getOperator());
        param.add(user.getAddress());

        // 食品信息上链
        String addTraceInfoByDistributor = weBASEUtils.funcPost(user.getUserName(), "addTraceInfoByDistributor", param);
        JSONObject res = JSONUtil.parseObj(addTraceInfoByDistributor);
        if (!res.get("message").equals("Success")) {
            return R.error(407, res.get("message").toString());
        }

        // 更新食品信息
        UpdateWrapper<Food> foodUpdateWrapper = new UpdateWrapper<>();
        foodUpdateWrapper.eq("trace_number", food.getTraceNumber());
        foodUpdateWrapper.set("status", 2);
        foodUpdateWrapper.set("quality", food.getQuality());
        foodMapper.update(null, foodUpdateWrapper);
        userAndFood.setTxHash((String) res.get("transactionHash"));
        userAndFood.setBlockNumber((String) res.get("blockNumber"));

        // 设置外键, 这里应该处理第三个表
        userAndFood.setUserId(user.getId());
        userAndFood.setTraceNumber(food.getTraceNumber());
        userAndFoodService.setUidAndFid(userAndFood);

        return R.ok();
    }

    @Override
    public R addTraceInfoByRetailer(FoodBO foodBO, String token) {
        if (token == null) {
            return R.error(402, "用户未登录!");
        }
        if (redisUtils.get(token) == null) {
            return R.error(502, "身份令牌已过期!");
        }

        // 处理两个表的实体类
        Food food = new Food();
        UserAndFood userAndFood = new UserAndFood();

        food.setTraceNumber(foodBO.getTraceNumber());
        food.setStatus(3);
        food.setQuality(foodBO.getQuality());

        userAndFood.setOrigin(foodBO.getOrigin());
        userAndFood.setTimestamp(foodBO.getTimestamp());
        userAndFood.setOperator(foodBO.getOperator());

        // 查询用户的信息
        QueryWrapper<User> userQueryWrapper = new QueryWrapper<>();
        userQueryWrapper.eq("user_name", (redisUtils.get(token)));
        User user = userMapper.selectOne(userQueryWrapper);

        List<java.io.Serializable> param = new ArrayList<>();
        param.add(food.getTraceNumber());
        param.add(userAndFood.getOrigin());
        param.add(userAndFood.getTimestamp());
        param.add(food.getQuality());
        param.add(userAndFood.getOperator());
        param.add(user.getAddress());

        // 食品信息上链
        String addTraceInfoByRetailer = weBASEUtils.funcPost(user.getUserName(), "addTraceInfoByRetailer", param);
        JSONObject res = JSONUtil.parseObj(addTraceInfoByRetailer);
        if (!res.get("message").equals("Success")) {
            return R.error(407, res.get("message").toString());
        }

        // 更新食品信息
        UpdateWrapper<Food> foodUpdateWrapper = new UpdateWrapper<>();
        foodUpdateWrapper.eq("trace_number", food.getTraceNumber());
        foodUpdateWrapper.set("status", 3);
        foodUpdateWrapper.set("quality", food.getQuality());
        foodMapper.update(null, foodUpdateWrapper);
        userAndFood.setTxHash((String) res.get("transactionHash"));
        userAndFood.setBlockNumber((String) res.get("blockNumber"));

        // 设置外键, 这里应该处理第三个表
        userAndFood.setUserId(user.getId());
        userAndFood.setTraceNumber(food.getTraceNumber());
        userAndFoodService.setUidAndFid(userAndFood);

        return R.ok();
    }

    @Override
    public R getTraceInfo(String traceNumber, String token) {
        if (token == null) {
            return R.error(402, "用户未登录!");
        }
        if (redisUtils.get(token) == null) {
            return R.error(502, "身份令牌已过期!");
        }

        // 查询用户的信息
        QueryWrapper<User> userQueryWrapper = new QueryWrapper<>();
        userQueryWrapper.eq("user_name", (redisUtils.get(token)));
        User user = userMapper.selectOne(userQueryWrapper);

        List<String> param = new ArrayList<>();
        param.add(traceNumber);

        String result = weBASEUtils.funcPost(user.getUserName(), "getFoodTrace", param);
        JSONArray array = JSONUtil.parseArray(result);
        QueryWrapper<Food> foodQueryWrapper = new QueryWrapper<>();
        foodQueryWrapper.eq("trace_number", traceNumber);
        Food food = foodMapper.selectOne(foodQueryWrapper);
        QueryWrapper<UserAndFood> userAndFoodQueryWrapper = new QueryWrapper<>();
        userAndFoodQueryWrapper.eq("trace_number", traceNumber);
        List<UserAndFood> userAndFoods = userAndFoodMapper.selectList(userAndFoodQueryWrapper);

        List<HashMap<String, Object>> list = new ArrayList<>();
        for (int k = 0; k < array.size(); k++) {
            for (int i = 0; i < array.getJSONArray(k).size(); i++) {
                if (i == 0) {
                    HashMap<String, Object> map = new HashMap<String, Object>();
                    map.put("traceNumber", traceNumber);
                    map.put("foodName", array.getJSONArray(k).getJSONArray(i).get(0));
                    map.put("origin", array.getJSONArray(k).getJSONArray(i).get(1));
                    map.put("timestamp", array.getJSONArray(k).getJSONArray(i).get(2));
                    map.put("quality", array.getJSONArray(k).getJSONArray(i).get(3));
                    map.put("status", array.getJSONArray(k).getJSONArray(i).get(4));
                    map.put("batches", array.getJSONArray(k).getJSONArray(i).get(5));
                    map.put("operator", array.getJSONArray(k).getJSONArray(i).get(6));
                    map.put("address", array.getJSONArray(k).getJSONArray(i).get(7));
                    map.put("expiration", food.getExpiration());
                    map.put("specification", food.getSpecification());
                    map.put("txhash",userAndFoods.get(i).getTxHash());
                    map.put("blocknumber",userAndFoods.get(i).getBlockNumber());
                    list.add(map);
                } else {
                    HashMap<String, Object> map = new HashMap<String, Object>();
                    map.put("traceNumber", traceNumber);
                    map.put("origin", array.getJSONArray(k).getJSONArray(i).get(1));
                    map.put("timestamp", array.getJSONArray(k).getJSONArray(i).get(2));
                    map.put("quality", array.getJSONArray(k).getJSONArray(i).get(3));
                    map.put("status", array.getJSONArray(k).getJSONArray(i).get(4));
                    map.put("operator", array.getJSONArray(k).getJSONArray(i).get(6));
                    map.put("address", array.getJSONArray(k).getJSONArray(i).get(7));
                    map.put("expiration", food.getExpiration());
                    map.put("specification", food.getSpecification());
                    map.put("txhash",userAndFoods.get(i).getTxHash());
                    map.put("blocknumber",userAndFoods.get(i).getBlockNumber());
                    list.add(map);
                }
            }
        }

        HashMap<String, List<HashMap<String, Object>>> map = new HashMap<String, List<HashMap<String, Object>>>();
        map.put("trace", list);

        return R.ok().put("data", map);
    }

    @Override
    public R getFoodInfo(String traceNumber, String token) {
        if (token == null) {
            return R.error(402, "用户未登录!");
        }
        if (redisUtils.get(token) == null) {
            return R.error(502, "身份令牌已过期!");
        }

        // 查询溯源码对应的食品信息
        QueryWrapper<Food> foodQueryWrapper = new QueryWrapper<>();
        foodQueryWrapper.eq("trace_number", traceNumber);
        foodQueryWrapper.isNotNull("food_name");
        Food food = foodMapper.selectOne(foodQueryWrapper);

        // 查询关联表
        QueryWrapper<UserAndFood> userAndFoodQueryWrapper = new QueryWrapper<>();
        userAndFoodQueryWrapper.eq("trace_number", traceNumber);
        List<UserAndFood> userAndFoods = userAndFoodMapper.selectList(userAndFoodQueryWrapper);

        // 查询用户的信息
        List<User> userList = new ArrayList<>();
        for (int i = 0; i < userAndFoods.size(); i++) {
            QueryWrapper<User> userQueryWrapper = new QueryWrapper<>();
            userQueryWrapper.eq("id", userAndFoods.get(i).getUserId());
            User user = userMapper.selectOne(userQueryWrapper);
            userList.add(user);
        }
        food.setUsers(userList);

        HashMap<String, Food> map = new HashMap<String, Food>();
        map.put("food", food);
        return R.ok().put("data", map);
    }

    @Override
    public R getFoodByTime(String minTime, String maxTime, int current, int size, String token) {
        if (token == null) {
            return R.error(402, "用户未登录!");
        }
        if (redisUtils.get(token) == null) {
            return R.error(502, "身份令牌已过期!");
        }

        com.baomidou.mybatisplus.extension.plugins.pagination.Page<UserAndFood> page = new com.baomidou.mybatisplus.extension.plugins.pagination.Page<>(current, size);
        QueryWrapper<User> userQueryWrapper = new QueryWrapper<>();
        userQueryWrapper.select("id").eq("user_name",redisUtils.get(token));
        User user = userMapper.selectOne(userQueryWrapper);
        QueryWrapper<UserAndFood> userAndFoodQueryWrapper = new QueryWrapper<>();
        userAndFoodQueryWrapper.select("*, GROUP_CONCAT(DISTINCT trace_number)").ge("timestamp", minTime).le("timestamp", maxTime).eq("user_id",user.getId()).groupBy("trace_number").orderByDesc("id");
        Page<UserAndFood> userAndFoodPage = userAndFoodMapper.selectPage(page, userAndFoodQueryWrapper);
        List<UserAndFood> userAndFoods = userAndFoodPage.getRecords();

        List foods = new ArrayList<>();
        for (UserAndFood userAndFood : userAndFoods) {
            QueryWrapper<Food> foodQueryWrapper = new QueryWrapper<>();
            foodQueryWrapper.eq("trace_number", userAndFood.getTraceNumber());
            Food food = foodMapper.selectOne(foodQueryWrapper);
            if (food == null) {
                continue;
            }

            HashMap<String, java.io.Serializable> foodMap = new HashMap<String, java.io.Serializable>();
            foodMap.put("traceNumber", food.getTraceNumber());
            foodMap.put("foodName", food.getFoodName());
            foodMap.put("batches", food.getBatches());
            foodMap.put("status", food.getStatus());
            foodMap.put("quality", food.getQuality());
            foodMap.put("timestamp", userAndFood.getTimestamp());
            foodMap.put("operator", userAndFood.getOperator());
            foods.add(foodMap);
        }

        HashMap<String, Object> map = new HashMap<String, Object>();
        map.put("food", foods);
        map.put("pages", page.getPages());
        map.put("total", page.getTotal());

        return R.ok().put("data", map);
    }

    @Override
    public R getAllFoodByTime(String minTime, String maxTime, String token) {
        if (token == null) {
            return R.error(402, "用户未登录!");
        }
        if (redisUtils.get(token) == null) {
            return R.error(502, "身份令牌已过期!");
        }
        QueryWrapper<User> userQueryWrapper = new QueryWrapper<>();
        userQueryWrapper.select("id").eq("user_name",redisUtils.get(token));
        User user = userMapper.selectOne(userQueryWrapper);
        QueryWrapper<UserAndFood> userAndFoodQueryWrapper = new QueryWrapper<>();
        userAndFoodQueryWrapper.select("trace_number, operator, timestamp, GROUP_CONCAT(DISTINCT trace_number)").ge("timestamp", minTime).lt("timestamp", maxTime).eq("user_id",user.getId()).groupBy("trace_number");
        List<UserAndFood> userAndFoods = userAndFoodMapper.selectList(userAndFoodQueryWrapper);

        List foods = new ArrayList<>();
        for (UserAndFood userAndFood : userAndFoods) {
            QueryWrapper<Food> foodQueryWrapper = new QueryWrapper<>();
            foodQueryWrapper.eq("trace_number", userAndFood.getTraceNumber());
            Food food = foodMapper.selectOne(foodQueryWrapper);
            if (food == null) {
                continue;
            }
            HashMap<String, java.io.Serializable> foodMap = new HashMap<String, java.io.Serializable>();
            foodMap.put("traceNumber", food.getTraceNumber());
            foodMap.put("foodName", food.getFoodName());
            foodMap.put("batches", food.getBatches());
            foodMap.put("status", food.getStatus());
            foodMap.put("quality", food.getQuality());
            foodMap.put("timestamp", userAndFood.getTimestamp());
            foodMap.put("operator", userAndFood.getOperator());
            foods.add(foodMap);
        }

        HashMap<String, List<Object>> map = new HashMap<String, List<Object>>();
        map.put("food", foods);

        return R.ok().put("data", map);
    }

    @Override
    public R getAllFoodByTimePC(int type, String token) {
        if (token == null) {
            return R.error(402, "用户未登录!");
        }
        if (redisUtils.get(token) == null) {
            return R.error(502, "身份令牌已过期!");
        }

        List<Map<String, Object>> userAndFoods = new ArrayList<>();
        if (type == 1) {
            userAndFoods = userAndFoodMapper.getOneWeekNumber();
        } else if (type == 2) {
            userAndFoods = userAndFoodMapper.getOneMonthNumber();
        } else if (type == 3) {
            userAndFoods = userAndFoodMapper.getOneYearNumber();
        }

        HashMap<String, List<Map<String, Object>>> map = new HashMap<>();
        map.put("number", userAndFoods);

        return R.ok().put("data", map);
    }

    @Override
    public R getFoodByTraceNumber(String traceNumber, String token) {
        if (token == null) {
            return R.error(402, "用户未登录!");
        }
        if (redisUtils.get(token) == null) {
            return R.error(502, "身份令牌已过期!");
        }

        // 查询溯源码对应的食品信息
        QueryWrapper<Food> foodQueryWrapper = new QueryWrapper<>();
        foodQueryWrapper.eq("trace_number", traceNumber);
        Food food = foodMapper.selectOne(foodQueryWrapper);
        if (food == null) {
            return R.error(400, "溯源码不存在!");
        }

        QueryWrapper<User> userQueryWrapper = new QueryWrapper<>();
        userQueryWrapper.eq("user_name", redisUtils.get(token));
        User user = userMapper.selectOne(userQueryWrapper);

        // 查询关联表
        QueryWrapper<UserAndFood> userAndFoodQueryWrapper = new QueryWrapper<>();
        userAndFoodQueryWrapper.eq("trace_number", traceNumber);
        userAndFoodQueryWrapper.eq("user_id", user.getId());
        List<UserAndFood> userAndFoods = userAndFoodMapper.selectList(userAndFoodQueryWrapper);

        if (userAndFoods.size() == 0) {
            return R.error(400, "未上传该商品信息!");
        }

        HashMap<String, String> map = new HashMap<>();
        map.put("traceNumber", food.getTraceNumber());
        map.put("foodName", food.getFoodName());

        return R.ok().put("data", map);
    }

    @Override
    public R getFoodByBatches(String batches, String token) {
        if (token == null) {
            return R.error(402, "用户未登录!");
        }
        if (redisUtils.get(token) == null) {
            return R.error(502, "身份令牌已过期!");
        }

        QueryWrapper<User> userQueryWrapper = new QueryWrapper<>();
        userQueryWrapper.eq("user_name", redisUtils.get(token));
        User user = userMapper.selectOne(userQueryWrapper);

        // 查询关联表
        QueryWrapper<UserAndFood> userAndFoodQueryWrapper = new QueryWrapper<>();
        userAndFoodQueryWrapper.eq("user_id", user.getId());
        List<UserAndFood> userAndFoods = userAndFoodMapper.selectList(userAndFoodQueryWrapper);

        List foods = new ArrayList<>();
        for (UserAndFood userAndFood : userAndFoods) {
            // 查询批次号对应的食品信息
            QueryWrapper<Food> foodQueryWrapper = new QueryWrapper<>();
            foodQueryWrapper.eq("batches", batches);
            foodQueryWrapper.eq("trace_number", userAndFood.getTraceNumber());
            Food food = foodMapper.selectOne(foodQueryWrapper);
            if (food == null) {
                continue;
            }
            HashMap<String, java.io.Serializable> foodMap = new HashMap<String, java.io.Serializable>();
            foodMap.put("traceNumber", food.getTraceNumber());
            foodMap.put("foodName", food.getFoodName());
            foodMap.put("batches", food.getBatches());
            foodMap.put("status", food.getStatus());
            foodMap.put("quality", food.getQuality());
            foodMap.put("timestamp", userAndFood.getTimestamp());
            foodMap.put("operator", userAndFood.getOperator());
            foods.add(foodMap);
        }

        if (foods == null) {
            return R.error(400, "批次号不存在!");
        }

        HashMap<String, List<Object>> map = new HashMap<>();
        map.put("foods", foods);

        return R.ok().put("data", map);

    }

    @Override
    public R getFoodByBatchesPAGE(String batches, int current, int size, String token) {

        return null;
    }

    @Override
    public R removeAllFood(String userName) {
        List<Food> foodList = foodMapper.selectList(null);
        for (Food food:
             foodList) {
            List<String> param = new ArrayList<String>();
            param.add(food.getTraceNumber());
            weBASEUtils.funcPost("admin", "removeFoodInfo", param);
        }
        foodMapper.delete(null);
        userAndFoodMapper.delete(null);
        return R.ok();
    }

}
