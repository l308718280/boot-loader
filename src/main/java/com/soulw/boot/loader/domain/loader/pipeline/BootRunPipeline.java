package com.soulw.boot.loader.domain.loader.pipeline;

import com.soulw.boot.loader.domain.loader.BasePipeline;
import com.soulw.boot.loader.domain.loader.Context;
import com.soulw.boot.loader.domain.loader.FatJarConfig;
import com.soulw.boot.loader.domain.loader.MavenVO;
import com.soulw.boot.loader.domain.loader.Pipeline;
import com.soulw.boot.loader.domain.loader.exception.BootException;
import com.soulw.boot.loader.domain.loader.exception.ContainerException;
import lombok.Getter;
import lombok.Value;
import lombok.extern.slf4j.Slf4j;
import org.springframework.boot.loader.JarLauncher;
import org.springframework.stereotype.Component;

import java.lang.reflect.Method;
import java.util.Objects;
import java.util.concurrent.CountDownLatch;
import java.util.concurrent.atomic.AtomicReference;

/**
 * Created by SoulW on 2024/3/27.
 *
 * @author SoulW
 * @since 2024/3/27 16:40
 */
@Slf4j
@Getter
@Component
public class BootRunPipeline extends BasePipeline {

    private static final String FAT_JAR_LAUNCHER = JarLauncher.class.getCanonicalName();

    @Override
    protected void doChain(Context context, Pipeline next) throws Exception {
        MavenVO mavenVO = context.getConfig().getMavenVo();
        String uniqueName = context.getConfig().getUniqueName();

        Class<?> launcher = loadLauncher(context.getAppClassLoader(), context.getConfig());

        Method method = launcher.getDeclaredMethod("main", String[].class);
        if (!method.isAccessible()) {
            method.setAccessible(true);
        }

        final CountDownLatch latch = new CountDownLatch(1);
        final AtomicReference<Throwable> exception = new AtomicReference<>();
        Thread startupThread = applyDaemonStartupThread(uniqueName, new BootRunner(context, () -> {
                    try {
                        method.invoke(null, new Object[]{context.getFinalArgs()});
                    } catch (Throwable e) {
                        exception.set(e);
                    } finally {
                        latch.countDown();
                    }
                }),
                context.getAppClassLoader());
        startupThread.start();
        context.setStartupThread(startupThread);
        latch.await();

        if (Objects.nonNull(exception.get())) {
            throw new ContainerException("startup failed: " + mavenVO.toMavenLocation(), exception.get());
        }

        log.info("BootRunPipeline#doChain launch jar [{}] success", mavenVO.toJarFileName());
    }

    private Thread applyDaemonStartupThread(String uniqueName, Runnable runnable, ClassLoader classLoader) {
        Thread thread = new Thread(runnable);
        thread.setName("BootRunPipeline-" + uniqueName);
        thread.setDaemon(true);
        thread.setContextClassLoader(classLoader);
        thread.setUncaughtExceptionHandler((t, e) -> log.error(e.getMessage(), e));
        thread.setPriority(Thread.NORM_PRIORITY);
        return thread;
    }

    /**
     * 启动线程
     *
     * @author Soulw
     */
    @Value
    public static class BootRunner implements Runnable {
        Context context;
        Runnable delegate;

        @Override
        public void run() {
            try {
                // step1. 执行依赖
                delegate.run();

                // step2. 永久睡眠
                while (!context.getClosed().get()) {
                    synchronized (this) {
                        this.wait();
                    }
                }
            } catch (Exception e) {
                throw new BootException("BootRunner failed", e);
            }
        }
    }

    private Class<?> loadLauncher(ClassLoader appClassLoader, FatJarConfig config) {
        try {
            return appClassLoader.loadClass(FAT_JAR_LAUNCHER);
        } catch (Exception e) {
            throw new BootException("不是SpringBoot的可运行FatJar？" + config.getMavenVo().toMavenLocation(), e);
        }
    }

    @Override
    public int getOrder() {
        return 10000;
    }
}
