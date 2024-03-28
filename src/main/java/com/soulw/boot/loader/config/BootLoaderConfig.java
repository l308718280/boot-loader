package com.soulw.boot.loader.config;

import com.soulw.boot.loader.domain.loader.FatJarConfig;
import lombok.Data;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.context.annotation.Configuration;

import java.util.List;

/**
 * Created by SoulW on 2024/3/28.
 *
 * @author SoulW
 * @since 2024/3/28 10:18
 */
@Data
@Configuration
@ConfigurationProperties(prefix = "boot.loader")
public class BootLoaderConfig {
    /**
     * 是否启用
     */
    private Boolean enabled;
    /**
     * 待启动的Jar包
     */
    private List<FatJarConfig> fatJars;
    /**
     * 禁用的pipeline, 类全路径
     */
    private List<String> deprecatePiepelineClassNames;

}
