package com.soulw.boot.loader.domain.loader;

import lombok.Data;

import java.io.File;

/**
 * Created by SoulW on 2024/3/27.
 *
 * @author SoulW
 * @since 2024/3/27 16:54
 */
@Data
public class MavenVO {
    private String groupId;
    private String artifactId;
    private String version;

    /**
     * 将Maven坐标转换为路径
     *
     * @return 返回Maven坐标对应的路径
     */
    public String toMavenPath() {
        return String.join(File.separator,
                groupId.replace('.', File.separatorChar),
                artifactId,
                version);
    }

    /**
     * 将Maven坐标转换为Maven位置
     *
     * @return 返回Maven位置字符串
     */
    public String toMavenLocation() {
        return String.join(":", groupId, artifactId, version);
    }

    /**
     * 将artifactId和version拼接成Jar文件名
     *
     * @return 返回拼接后的Jar文件名
     */
    public String toJarFileName() {
        return String.join("-", artifactId, version);
    }
}
