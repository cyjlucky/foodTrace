package com.foodtrace.result;

public class Result<T> {
    private int code;
    private String msg;
    private T data;

    public Result(int code, String msg, T data) {
        this.code = code;
        this.msg = msg;
        this.data = data;
    }

    /**
     * 统一返回内容
     * */
    //成功:200
    public static Result SUCCESS = new Result(200, "正常", "正常");
    public static Result PARAM_EMPTY = new Result(404001, "请求参数缺失!", "请求参数缺失!");
    public static Result QUERY_EMPTY = new Result(404002, "查询内容不存在!", "查询内容不存在!");
    public static Result NOT_LOGIN = new Result(400003, "用户未登录!","用户未登录!");
    public static Result PARAM_ERROR = new Result(400003, "账户或密码错误!","账户或密码错误!");
    public static Result QUERY_EXISTS = new Result(500001, "信息已存在！", "信息已存在！");
    public static Result TOKEN_EMPTY = new Result(500003, "Token已失效","Token已失效");



    public int getCode() {
        return code;
    }

    public void setCode(int code) {
        this.code = code;
    }

    public String getMsg() {
        return msg;
    }

    public void setMsg(String msg) {
        this.msg = msg;
    }

    public T getData() {
        return data;
    }

    public void setData(T data) {
        this.data = data;
    }
}
