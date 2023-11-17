package com.foodtrace.result.R;

import org.apache.http.HttpStatus;

import java.util.HashMap;
import java.util.Map;

/**
 * 统一返回数据格式
 */
public class R extends HashMap<String, Object> {
    private static final long serialVersionUID = 1L;

    public R() {
        put("code", 200);
        put("msg", "success");
    }

    /**
     * 返回错误信息
     * @return R对象，包含code和msg字段
     */
    public static R error() {
        return error(HttpStatus.SC_INTERNAL_SERVER_ERROR, "未知异常，请联系管理员");
    }

    /**
     * 返回错误信息
     * @param msg 错误信息
     * @return R对象，包含code和msg字段
     */
    public static R error(String msg) {
        return error(HttpStatus.SC_INTERNAL_SERVER_ERROR, msg);
    }

    /**
     * 返回错误信息
     * @param code 错误码
     * @param msg 错误信息
     * @return R对象，包含code和msg字段
     */
    public static R error(int code, String msg) {
        R r = new R();
        r.put("code", code);
        r.put("msg", msg);
        return r;
    }

    /**
     * 返回成功信息
     * @param msg 成功信息
     * @return R对象，包含msg字段
     */
    public static R ok(String msg) {
        R r = new R();
        r.put("msg", msg);
        return r;
    }

    /**
     * 返回成功信息
     * @param map 包含成功信息的键值对
     * @return R对象，包含map中的所有键值对
     */
    public static R ok(Map<String, Object> map) {
        R r = new R();
        r.putAll(map);
        return r;
    }

    /**
     * 返回成功信息
     * @return R对象，只包含msg字段，值为"success"
     */
    public static R ok() {
        return new R();
    }

    /**
     * 添加键值对
     * @param key 键
     * @param value 值
     * @return 当前R对象
     */
    public R put(String key, Object value) {
        super.put(key, value);
        return this;
    }
}
