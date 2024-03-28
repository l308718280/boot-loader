package com.soulw.boot.loader.domain.loader;

import lombok.Data;
import lombok.experimental.Accessors;
import org.apache.commons.lang3.StringUtils;

import java.util.List;
import java.util.concurrent.atomic.AtomicInteger;

/**
 * Created by SoulW on 2024/3/27.
 *
 * @author SoulW
 * @since 2024/3/27 16:54
 */
@Data
@Accessors(chain = true)
public class FatJarConfig {
    /**
     * 序号
     */
    private static final AtomicInteger sequence = new AtomicInteger();
    /**
     * 全局唯一标识
     */
    private final Integer id = sequence.incrementAndGet();
    /**
     * maven坐标
     */
    private MavenVO mavenVo;
    /**
     * 指定端口启动
     */
    private Integer port;
    /**
     * 附加参数，spring的参考: "--server.port=8081"
     */
    private List<String> appendArgs;
    /**
     * 唯一名称, 会注册到JMX中, 不设置默认有值
     */
    private String uniqueName;

    /**
     * 获取唯一名称
     *
     * @return 唯一名称
     */
    public String getUniqueName() {
        if (StringUtils.isBlank(uniqueName)) {
            uniqueName = String.join("", mavenVo.getArtifactId(), String.valueOf(id));
        }
        return uniqueName;
    }

}
