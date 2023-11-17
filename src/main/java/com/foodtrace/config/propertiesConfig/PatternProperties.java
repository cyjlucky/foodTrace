package com.foodtrace.config.propertiesConfig;

import com.alibaba.nacos.api.config.annotation.NacosConfigurationProperties;
import lombok.Data;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.stereotype.Component;


/**
 * nacos 配置中心 配置 读取
 */
@Data
@Component
public class PatternProperties {
    @Value("${pattern.contractAddress}")
    private String contractAddress;

    @Value("${pattern.myWeBASEAndPort}")
    private String myWeBASEAndPort;

    @Value("${pattern.API.privateKey}")
    private String privateKey;

    @Value("${pattern.API.trans}")
    private String trans;

}
