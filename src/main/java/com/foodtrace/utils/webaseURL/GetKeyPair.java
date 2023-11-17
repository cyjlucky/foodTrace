package com.foodtrace.utils.webaseURL;

import java.io.IOException;
import java.net.URI;
import java.net.http.HttpClient;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse;

import com.alibaba.nacos.shaded.com.google.gson.Gson;
import com.foodtrace.config.propertiesConfig.PatternProperties;
import com.foodtrace.vo.KeyPairResult;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import javax.annotation.PostConstruct;

@Component
public class GetKeyPair {

    @Autowired
    private PatternProperties properties;
    String url;

    @PostConstruct
    public void init() {
        url = properties.getMyWeBASEAndPort() + properties.getPrivateKey();
    }

    public KeyPairResult getKey(String userName) throws IOException, InterruptedException {
        // 参数
        String type = "2"; // 外部用户
        String signUserId = userName;
        String appId = "1";

        // 构建GET请求
        HttpRequest request = HttpRequest.newBuilder()
                .uri(URI.create(url + "?type=" + type + "&userName=" + userName + "&signUserId=" + signUserId + "&appId=" + appId))
                .build();

        // 发送请求
        HttpClient client = HttpClient.newHttpClient();
        HttpResponse<String> response = client.send(request, HttpResponse.BodyHandlers.ofString());

        // 解析响应
        String result = response.body();

        Gson gson = new Gson();
        KeyPairResult keyPair = gson.fromJson(result, KeyPairResult.class);

        return keyPair;
    }

}

