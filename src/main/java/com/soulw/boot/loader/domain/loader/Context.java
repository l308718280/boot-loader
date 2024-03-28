package com.soulw.boot.loader.domain.loader;

import lombok.Data;

import java.util.concurrent.atomic.AtomicBoolean;

/**
 * Created by SoulW on 2024/3/28.
 *
 * @author SoulW
 * @since 2024/3/28 9:45
 */
@Data
public class Context {
    /**
     * 用于加载FatJar
     */
    private ClassLoader appClassLoader;
    /**
     * Jar路径
     */
    private String jarPath;
    /**
     * 配置
     */
    private FatJarConfig config;
    /**
     * 当前请求的args
     */
    String[] requestArgs;
    /**
     * 最终执行的参数
     */
    String[] finalArgs;
    /**
     * 启动线程
     */
    Thread startupThread;
    /**
     * 关闭上下文开关
     */
    AtomicBoolean closed = new AtomicBoolean(false);
}
