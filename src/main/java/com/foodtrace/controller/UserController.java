package com.foodtrace.controller;

import com.foodtrace.result.R.R;
import com.foodtrace.service.user.UserService;
import com.foodtrace.utils.RedisUtils;
import com.foodtrace.utils.TokenUtils;
import com.foodtrace.vo.User;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

import java.io.IOException;

/**
 *
 */
@RestController
@RequestMapping("/user")
public class UserController {

    @Autowired
    private UserService userService;

    @Autowired
    private RedisUtils redisUtils;

    @PostMapping("/login")
    @ResponseBody
    public R login(@RequestBody User user) {
        return userService.login(user);
    }

    @PostMapping("/register")
    @ResponseBody
    public R register(@RequestBody User user) throws IOException, InterruptedException {
        return userService.register(user);
    }

    @GetMapping("/getAllFoodByUser")
    @ResponseBody
    public R allFoodByUser(@RequestHeader(required = false) String token) {
        return userService.allFoodByUser(token);
    }

    @GetMapping("/getUserInfo")
    @ResponseBody
    public R userInfo(@RequestHeader(required = false) String token){
        return userService.userInfo(token);
    }

    @GetMapping("/isRetailer")
    @ResponseBody
    public R isRetailer(String address){
        return userService.isRetailer(address);
    }


    /**
     * 检查 token 是否存活
     *
     * @param userName 需要检查的 用户
     * @return
     */
    @GetMapping("/verify")
    public R Verify(@RequestParam String userName) {
        // 使用 用户名 从 Redis 获取 token 有空指针异常的可能性, 前端直接传入 token 进行验证则没有
        String token = redisUtils.get(userName);
        try {
            TokenUtils.tokenVerify(token);
        } catch (Exception e) {
            // 抛异常则代表 token 认证不通过,直接返回
            return R.error(400, "token 已经过期!");
        }
        return R.ok();
    }


}
