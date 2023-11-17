package com.foodtrace.service.user;

import cn.hutool.json.JSONObject;
import cn.hutool.json.JSONUtil;
import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.foodtrace.config.propertiesConfig.PatternProperties;
import com.foodtrace.mapper.FoodMapper;
import com.foodtrace.mapper.UserAndFoodMapper;
import com.foodtrace.mapper.UserMapper;
import com.foodtrace.result.R.R;
import com.foodtrace.utils.webaseURL.GetKeyPair;
import com.foodtrace.utils.RedisUtils;
import com.foodtrace.utils.TokenUtils;
import com.foodtrace.utils.webaseURL.WeBASEUtils;
import com.foodtrace.vo.Food;
import com.foodtrace.vo.KeyPairResult;
import com.foodtrace.vo.User;
import com.foodtrace.vo.UserAndFood;
import org.python.antlr.ast.Str;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.util.DigestUtils;

import java.io.IOException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.concurrent.TimeUnit;

@Service
public class UserServiceImpl extends ServiceImpl<UserMapper, User> implements UserService {

    @Autowired
    private UserMapper userMapper;

    @Autowired
    private FoodMapper foodMapper;

    @Autowired
    private UserAndFoodMapper userAndFoodMapper;

    @Autowired
    private RedisUtils redisUtils;

    @Autowired
    private WeBASEUtils weBASEUtils;

    @Autowired
    private GetKeyPair getKeyPair;

    @Override
    public R login(User user) {
        // 判断用户是否存在
        QueryWrapper<User> wrapper = new QueryWrapper<>();
        wrapper.eq("user_name", user.getUserName());
        wrapper.eq("password", DigestUtils.md5DigestAsHex(user.getPassword().getBytes()));
        user = userMapper.selectOne(wrapper);

        if (user == null) {
            return R.error(400, "账户或密码错误!");
        }
        if (user.getState() == 0){
            return R.error(400, "该账户已被禁用!");
        }

        // 生成 token
        String token = TokenUtils.sign(user);
        redisUtils.set(token, user.getUserName(), 1, TimeUnit.HOURS);
        long expireTime = System.currentTimeMillis();

        HashMap res = new HashMap();
        res.put("userId", user.getId());
        res.put("token", token);
        res.put("expireTime", expireTime); // 生成 token 时的 时间戳

        return R.ok().put("data", res);
    }

    @Override
    public R register(User user) throws IOException, InterruptedException {
        // 判断用户是否存在
        QueryWrapper<User> wrapper = new QueryWrapper<>();
        wrapper.eq("user_name", user.getUserName());
        if (userMapper.selectMaps(wrapper).size() > 0) {
            return R.error(500, "用户已存在!");
        }

        // md5 加密
        user.setPassword(DigestUtils.md5DigestAsHex(user.getPassword().getBytes()));

        // 生成公钥和地址
        KeyPairResult key = getKeyPair.getKey(user.getUserName());
        user.setAddress(key.getAddress());
        user.setPublicKey(key.getPublicKey());

        List param = new ArrayList();
        param.add(user.getAddress());
        param.add(user.getUserType());

        // 用户信息上链
        String accountRegister = weBASEUtils.funcPost(user.getUserName(), "account_register", param);
        JSONObject res = JSONUtil.parseObj(accountRegister);
        if (!res.get("message").equals("Success")){
            return R.error(500, "智能合约请求存在问题!");
        }

        user.setState(1);

        // 注册,将信息插入数据库
        userMapper.insert(user);

        return R.ok();
    }

    @Override
    public R allFoodByUser(String token) {
        if (token == null){
            return R.error(402, "用户未登录!");
        }
        if (redisUtils.get(token) == null) {
            return R.error(502, "身份令牌已过期!");
        }

        QueryWrapper<User> wrapper = new QueryWrapper<>();
        wrapper.eq("user_name", redisUtils.get(token));
        User user = userMapper.selectOne(wrapper);

        // 查询第三表
        QueryWrapper<UserAndFood> userAndFoodQueryWrapper = new QueryWrapper<>();
        userAndFoodQueryWrapper.eq("user_id", user.getId());
        List<UserAndFood> userAndFoods = userAndFoodMapper.selectList(userAndFoodQueryWrapper);

        // 查询对应的食品列表信息
        List<Food> foods = new ArrayList<>();
        for (UserAndFood userAndFood: userAndFoods){
            QueryWrapper<Food> foodQueryWrapper = new QueryWrapper<>();
            foodQueryWrapper.eq("trace_number", userAndFood.getTraceNumber());
            foods.add(foodMapper.selectOne(foodQueryWrapper));
        }

        HashMap map = new HashMap();
        map.put("foodlist", foods);

        return R.ok().put("data", map);
    }

    @Override
    public R userInfo(String token) {
        if (token == null){
            return R.error(402, "用户未登录!");
        }
        if (redisUtils.get(token) == null) {
            return R.error(502, "身份令牌已过期!");
        }

        QueryWrapper<User> wrapper = new QueryWrapper<>();
        wrapper.eq("user_name", redisUtils.get(token));
        wrapper.select("user_name", "company_name", "corporate", "location", "phone_number", "address", "public_key", "user_type");
        User user = userMapper.selectOne(wrapper);

        HashMap userInfo = new HashMap();
        userInfo.put("userName", user.getUserName());
        userInfo.put("companyName", user.getCompanyName());
        userInfo.put("corporate", user.getCorporate());
        userInfo.put("location", user.getLocation());
        userInfo.put("phoneNumber", user.getPhoneNumber());
        userInfo.put("address", user.getAddress());
        userInfo.put("publicKey", user.getPublicKey());
        userInfo.put("userType", user.getUserType());

        HashMap map = new HashMap();
        map.put("userInfo", userInfo);

        return R.ok().put("data", map);
    }

    @Override
    public R isRetailer(String address) {

        QueryWrapper<User> wrapper = new QueryWrapper<>();
        wrapper.eq("address", address);
        User user = userMapper.selectOne(wrapper);
        
        if (user.getUserType() != 3){
            return R.error(403, "该地址不是超市角色!");
        }

        HashMap map = new HashMap();
        map.put("userName", user.getUserName());

        return R.ok().put("data", map);
    }

    @Override
    public R updatePassword(String token,String oldPassword,String newPassword) {
        if (token == null){
            return R.error(402, "用户未登录!");
        }
        if (redisUtils.get(token) == null) {
            return R.error(502, "身份令牌已过期!");
        }
        QueryWrapper<User> wrapper = new QueryWrapper<>();
        wrapper.eq("user_name", redisUtils.get(token));
        wrapper.eq("password", DigestUtils.md5DigestAsHex(oldPassword.getBytes()));
        User user = userMapper.selectOne(wrapper);
        if (user == null) {
            return R.error(400, "原密码错误!");
        }
        user.setPassword(DigestUtils.md5DigestAsHex(newPassword.getBytes()));
        userMapper.updateById(user);
        return R.ok();
    }

    @Override
    public R updateUser(String token, User user) {
        if (token == null){
            return R.error(402, "用户未登录!");
        }
        if (redisUtils.get(token) == null) {
            return R.error(502, "身份令牌已过期!");
        }
        QueryWrapper<User> wrapper = new QueryWrapper<>();
        wrapper.eq("user_name", user.getUserName());
        userMapper.update(user,wrapper);
        return R.ok();
    }


}
