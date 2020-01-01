package com.run.gateway;

import com.alibaba.nacos.api.config.annotation.NacosValue;
import com.alibaba.nacos.spring.context.annotation.config.NacosPropertySource;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.boot.CommandLineRunner;
import org.springframework.stereotype.Component;
@Component
@NacosPropertySource(dataId = "com.run.gateway", groupId="DEFAULT_GROUP", autoRefreshed = true)
public class NacosConfigAnnoatationService implements CommandLineRunner {
    private static final Logger LOGGER = LoggerFactory.getLogger(NacosConfigAnnoatationService.class);
    /**
     * ${name:hello}:key=name,默认值=hello
     */
    @NacosValue(value = "${name:hello}", autoRefreshed = true)
    String name;

    @NacosValue(value = "${interest:world}", autoRefreshed = true)
    String interest;

    @Override
    public void run(String... args) throws Exception {
        LOGGER.info("[NacosConfigAnnoatationService]注解方式获取到的配置项目,name={},interest={}", name, interest);
    }
}
