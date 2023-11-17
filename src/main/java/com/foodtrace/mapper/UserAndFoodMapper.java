package com.foodtrace.mapper;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.foodtrace.vo.UserAndFood;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Select;

import java.util.List;
import java.util.Map;

@Mapper
public interface UserAndFoodMapper extends BaseMapper<UserAndFood> {

    @Select("SELECT a.date AS date, IFNULL(b.count,0) AS count\n" +
            "FROM \n" +
            "  (SELECT DATE_FORMAT(DATE(DATE_SUB(NOW(), INTERVAL @i:=@i+1 DAY)), '%Y-%m-%d') AS date\n" +
            "   FROM t_user_foodinfo,\n" +
            "   (SELECT @i:= 0) r\n" +
            "   WHERE @i < 7) a\n" +
            "LEFT JOIN\n" +
            "  (SELECT DATE_FORMAT(FROM_UNIXTIME(timestamp/1000), '%Y-%m-%d') AS date, COUNT(*) AS count\n" +
            "   FROM t_user_foodinfo\n" +
            "   WHERE timestamp >= UNIX_TIMESTAMP(DATE_SUB(NOW(), INTERVAL 7 DAY))*1000\n" +
            "   GROUP BY date) b\n" +
            "ON a.date = b.date\n" +
            "ORDER BY a.date;\n")
    List<Map<String, Object>> getOneWeekNumber();

    @Select("SELECT a.date AS date, IFNULL(b.count,0) AS count\n" +
            "FROM \n" +
            "  (SELECT DATE_FORMAT(DATE(DATE_SUB(NOW(), INTERVAL @i:=@i+1 DAY)), '%Y-%m-%d') AS date\n" +
            "   FROM t_user_foodinfo,\n" +
            "   (SELECT @i:= 0) r\n" +
            "   WHERE @i < 30) a\n" +
            "LEFT JOIN\n" +
            "  (SELECT DATE_FORMAT(FROM_UNIXTIME(timestamp/1000), '%Y-%m-%d') AS date, COUNT(*) AS count\n" +
            "   FROM t_user_foodinfo\n" +
            "   WHERE timestamp >= UNIX_TIMESTAMP(DATE_SUB(NOW(), INTERVAL 7 MONTH))*1000\n" +
            "   GROUP BY date) b\n" +
            "ON a.date = b.date\n" +
            "ORDER BY a.date;")
    List<Map<String, Object>> getOneMonthNumber();

    @Select("SELECT CONCAT(YEAR(FROM_UNIXTIME(timestamp/1000)), '-', LPAD(MONTH(FROM_UNIXTIME(timestamp/1000)), 2, '0')) AS date, COUNT(*) AS count\n" +
            "FROM t_user_foodinfo\n" +
            "WHERE timestamp >= UNIX_TIMESTAMP(DATE_SUB(NOW(), INTERVAL 1 YEAR))*1000\n" +
            "GROUP BY CONCAT(YEAR(FROM_UNIXTIME(timestamp/1000)), '-', LPAD(MONTH(FROM_UNIXTIME(timestamp/1000)), 2, '0'))\n" +
            "ORDER BY date;")
    List<Map<String, Object>> getOneYearNumber();



}
