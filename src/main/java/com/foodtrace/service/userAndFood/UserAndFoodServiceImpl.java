package com.foodtrace.service.userAndFood;

import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.foodtrace.mapper.UserAndFoodMapper;
import com.foodtrace.vo.UserAndFood;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

@Service
public class UserAndFoodServiceImpl extends ServiceImpl<UserAndFoodMapper, UserAndFood> implements UserAndFoodService {

    @Autowired
    private UserAndFoodMapper userAndFoodMapper;

    @Override
    public boolean setUidAndFid(UserAndFood userAndFood) {

        boolean ret = false;

        int insert = userAndFoodMapper.insert(userAndFood);
        if (insert > 0){
            ret = true;
        }

        return ret;
    }
}
