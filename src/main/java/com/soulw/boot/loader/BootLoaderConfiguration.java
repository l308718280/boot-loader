package com.soulw.boot.loader;

import com.soulw.boot.loader.config.BootLoaderConfig;
import com.soulw.boot.loader.template.PipelineTemplate;
import org.springframework.beans.factory.InitializingBean;
import org.springframework.boot.autoconfigure.AutoConfigureAfter;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.boot.autoconfigure.web.servlet.ServletWebServerFactoryAutoConfiguration;
import org.springframework.boot.context.properties.EnableConfigurationProperties;
import org.springframework.context.annotation.ComponentScan;
import org.springframework.context.annotation.Configuration;

import javax.annotation.Resource;

/**
 * Created by SoulW on 2024/3/27.
 *
 * @author SoulW
 * @since 2024/3/27 16:30
 */
@Configuration
@ConditionalOnProperty(prefix = "boot.loader", name = "enabled", havingValue = "true", matchIfMissing = true)
@EnableConfigurationProperties(BootLoaderConfig.class)
@AutoConfigureAfter(ServletWebServerFactoryAutoConfiguration.class)
@ComponentScan("com.soulw.boot.loader")
public class BootLoaderConfiguration implements InitializingBean {

    @Resource
    private PipelineTemplate pipelineTemplate;

    @Override
    public void afterPropertiesSet() throws Exception {
        pipelineTemplate.executeByDefault(new String[0]);
    }
}
