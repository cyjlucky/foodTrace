package com.foodtrace.utils.webaseURL;


import cn.hutool.json.JSONArray;
import cn.hutool.json.JSONObject;
import cn.hutool.json.JSONUtil;
import com.foodtrace.config.propertiesConfig.PatternProperties;
import org.apache.http.client.methods.CloseableHttpResponse;
import org.apache.http.client.methods.HttpPost;
import org.apache.http.entity.StringEntity;
import org.apache.http.impl.client.CloseableHttpClient;
import org.apache.http.impl.client.HttpClients;
import org.apache.http.util.EntityUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import javax.annotation.PostConstruct;
import java.io.IOException;
import java.nio.charset.Charset;
import java.util.List;

@Component
public class WeBASEUtils {

    @Autowired
    private PatternProperties properties;
    String webaseUrl;
    String contractAddress;

    @PostConstruct
    public void init() {
        webaseUrl = properties.getMyWeBASEAndPort() + properties.getTrans();
        contractAddress = properties.getContractAddress();
    }

    public static final String ABI = com.foodtrace.utils.IOUtils.readResourceAsString("abi/trace.abi");
    /**
     * 发送 post 请求调用链接口
     * @param signUserId
     * @param funcName
     * @param funcParam
     * @return 请求结果
     */
    public String funcPost(String signUserId, String funcName, List funcParam) {
        System.out.println(webaseUrl);

        JSONArray abiJSON = JSONUtil.parseArray(ABI);
        JSONObject data = JSONUtil.createObj();

        data.put("signUserId", signUserId);
        data.put("contractName", "Trace");
        data.put("contractAddress", contractAddress);
        data.put("funcName", funcName);
        data.put("contractAbi", abiJSON);
        data.put("funcParam", funcParam);
        data.put("groupId", "1");
        data.put("useAes", false);
        data.put("useCns", false);
        data.put("cnsName", "");

        String dataString = JSONUtil.toJsonStr(data);

        CloseableHttpClient httpClient = HttpClients.createDefault();
        HttpPost httpPost = new HttpPost(webaseUrl);
        httpPost.setHeader("Content-type", "application/json;charset=utf-8");

        StringEntity entity = new StringEntity(dataString, Charset.forName("utf-8"));
        entity.setContentEncoding("utf-8");
        entity.setContentType("application/json");

        httpPost.setEntity(entity);

        CloseableHttpResponse httpResponse;

        String result = null;
        try {
            httpResponse = httpClient.execute(httpPost);
            result = EntityUtils.toString(httpResponse.getEntity(), "utf-8");
        } catch (IOException e) {
            e.printStackTrace();
        }
        System.out.println(result);
        return result;
    }
}
