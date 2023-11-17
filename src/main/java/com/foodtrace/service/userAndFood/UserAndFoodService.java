package com.foodtrace.service.userAndFood;

import com.baomidou.mybatisplus.extension.service.IService;
import com.foodtrace.vo.UserAndFood;

public interface UserAndFoodService extends IService<UserAndFood> {
    boolean setUidAndFid(UserAndFood userAndFood);
}
