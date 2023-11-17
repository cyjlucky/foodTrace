package com.foodtrace.mapper;

import cn.hutool.db.Page;
import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.foodtrace.result.R.R;
import com.foodtrace.vo.Food;
import org.apache.ibatis.annotations.Mapper;

@Mapper
public interface FoodMapper extends BaseMapper<Food> {
}
