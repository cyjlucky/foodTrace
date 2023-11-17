package com.foodtrace.mapper;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.foodtrace.result.R.R;
import com.foodtrace.vo.User;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Select;

import java.util.List;
import java.util.Map;

@Mapper
public interface UserMapper extends BaseMapper<User> {

}
