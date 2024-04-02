package com.soulw.boot.loader.domain.loader.pipeline;

import com.soulw.boot.loader.domain.loader.BasePipeline;
import com.soulw.boot.loader.domain.loader.Context;
import com.soulw.boot.loader.domain.loader.Pipeline;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;
import sun.misc.Launcher;

import java.io.File;
import java.lang.reflect.Method;
import java.net.URL;
import java.net.URLClassLoader;
import java.util.concurrent.atomic.AtomicBoolean;

/**
 * Created by SoulW on 2024/4/1.
 *
 * @author SoulW
 * @since 2024/4/1 19:11
 */
@Component
@Slf4j
public class InstallBootLoaderPipeline extends BasePipeline {
    public static final String FILE_PROTOCOL = "file://";

    private static final AtomicBoolean installed = new AtomicBoolean(false);

    @Override
    protected void doChain(Context context, Pipeline next) throws Exception {
        if (installed.compareAndSet(false, true)) {
            Launcher launcher = Launcher.getLauncher();
            URLClassLoader appClassLoader = (URLClassLoader) launcher.getClassLoader();

            Method addURL = URLClassLoader.class.getDeclaredMethod("addURL", URL.class);
            if (!addURL.isAccessible()) {
                addURL.setAccessible(true);
            }

            addURL.invoke(appClassLoader, new URL(FILE_PROTOCOL + File.separator + context.getBootLoaderJarPath()));
            log.info("InstallBootLoaderPipeline#doChain success");
        }

        next.chain(context);
    }

    @Override
    public int getOrder() {
        return PipelineOrderEnum.INSTALL_BOOT_LOADER_PIPELINE.getCode();
    }
}
