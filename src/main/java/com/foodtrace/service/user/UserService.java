package com.foodtrace.service.user;

import com.baomidou.mybatisplus.extension.service.IService;
import com.foodtrace.result.R.R;
import com.foodtrace.vo.User;
import org.springframework.web.bind.annotation.RequestBody;

import java.io.IOException;
import java.util.HashMap;

public interface UserService extends IService<User> {
    R register(User user) throws IOException, InterruptedException;
    R login(User user);
    R allFoodByUser(String token);
    R userInfo(String token);
    R isRetailer(String address);
}
