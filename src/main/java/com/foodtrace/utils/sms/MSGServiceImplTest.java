package com.foodtrace.utils.sms;

import com.alibaba.fastjson.JSONObject;
import com.aliyun.dysmsapi20170525.Client;
import com.aliyun.dysmsapi20170525.models.SendSmsRequest;
import com.aliyun.dysmsapi20170525.models.SendSmsResponse;
import com.aliyun.teaopenapi.models.Config;
import com.google.gson.Gson;
import org.springframework.stereotype.Service;
import org.springframework.util.StringUtils;

import java.util.Map;

@Service("test")
public class MSGServiceImplTest implements MSGService {

    @Override
    public boolean send(Map map, String phone) {
        if (StringUtils.isEmpty(phone)) return false;

        Config config = new Config()
                // AccessKey ID
                .setAccessKeyId("LTAI5tC17RwCx6suFQQrtNJD")
                // AccessKey Secret
                .setAccessKeySecret("aiMolvFGFo4ndZDGgKTDENu6I5itoP");
        // 访问的域名
        config.endpoint = "dysmsapi.aliyuncs.com";
        Client client;
        try {
            client = new Client(config);
            SendSmsRequest request = new SendSmsRequest();

            request.setSignName("阿里云短信测试");//签名名称
            request.setTemplateCode("SMS_154950909");//模版Code
            request.setPhoneNumbers(phone);//电话号码
            //这里的参数是json格式的字符串
            request.setTemplateParam(JSONObject.toJSONString(map));
            SendSmsResponse response = client.sendSms(request);
            System.out.println("发送成功：" + new Gson().toJson(response));
            return true;
        } catch (Exception e) {
            e.printStackTrace();
            return false;
        }
    }
}


