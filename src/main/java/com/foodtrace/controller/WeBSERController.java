package com.foodtrace.controller;

import com.alibaba.nacos.shaded.com.google.gson.Gson;
import com.foodtrace.config.propertiesConfig.PatternProperties;
import com.foodtrace.result.R.R;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.ResponseBody;
import org.springframework.web.bind.annotation.RestController;

import java.io.IOException;
import java.net.URI;
import java.net.http.HttpClient;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse;
import java.util.HashMap;

@RequestMapping("/webase")
@RestController
public class WeBSERController {

    @Autowired
    private PatternProperties properties;

    @GetMapping("/getWebase")
    @ResponseBody
    public R getWebase(String url) throws IOException, InterruptedException {
        url = properties.getMyWeBASEAndPort() + url;
        HttpRequest request = HttpRequest.newBuilder()
                .uri(URI.create(url))
                .build();

        // 发送请求
        HttpClient client = HttpClient.newHttpClient();
        HttpResponse<String> response = client.send(request, HttpResponse.BodyHandlers.ofString());

        System.out.println(response);
        // 解析响应
        Gson gson = new Gson();

        HashMap map = new HashMap();
        map.put("resule", gson.fromJson(response.body(), Object.class));

        return R.ok().put("data", map);

    }

}
