package com.soulw.boot.loader.domain.loader.pipeline;

import com.google.common.base.Preconditions;
import com.soulw.boot.loader.domain.loader.BasePipeline;
import com.soulw.boot.loader.domain.loader.Context;
import com.soulw.boot.loader.domain.loader.MavenVO;
import com.soulw.boot.loader.domain.loader.Pipeline;
import com.soulw.boot.loader.domain.loader.finder.JarFinder;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;

import javax.annotation.Resource;
import javax.servlet.ServletContext;
import java.io.File;
import java.net.MalformedURLException;
import java.net.URL;
import java.net.URLClassLoader;
import java.util.Objects;

/**
 * Created by SoulW on 2024/3/28.
 *
 * @author SoulW
 * @since 2024/3/28 9:52
 */
@Slf4j
@Component
public class FindJarFilePipeline extends BasePipeline {

    private static final String PROTOCOL_FILE = "file://";
    private static final String BOOT_LOADER_GROUP_ID = "org.springframework.boot";
    private static final String BOOT_LOADER_ARTIFACT_ID = "spring-boot-loader";
    @Resource
    private ServletContext servletContext;

    @Override
    protected void doChain(Context context, Pipeline next) throws Exception {
        ClassLoader currentClassLoader = getClass().getClassLoader();
        ClassLoader extClassLoader = findExtClassLoader(currentClassLoader);
        Preconditions.checkNotNull(extClassLoader, "extClassLoader no found!!!");

        String jarPath = new JarFinder(context.getConfig().getMavenVo(), servletContext)
                .findJarPath();
        Preconditions.checkNotNull(jarPath, "jarPath no found!");

        String bootJarPath = new JarFinder(loadBootJarMaven(), servletContext).findJarPath();
        Preconditions.checkNotNull(bootJarPath, "bootJarPath no found!");

        context.setJarPath(jarPath);
        context.setBootLoaderJarPath(bootJarPath);
        context.setAppClassLoader(initAppClassLoader(jarPath, extClassLoader));

        log.info("InitPipeline#doChain startup success");
        next.chain(context);
    }

    private MavenVO loadBootJarMaven() {
        MavenVO vo = new MavenVO();
        vo.setGroupId(BOOT_LOADER_GROUP_ID);
        vo.setArtifactId(BOOT_LOADER_ARTIFACT_ID);
        vo.setVersion("");
        return vo;
    }

    private ClassLoader initAppClassLoader(String jarPath, ClassLoader extClassLoader) throws MalformedURLException {
        try {
            return new URLClassLoader(new URL[]{new URL(PROTOCOL_FILE + File.separator + jarPath)}, extClassLoader);
        } catch (Exception e) {
            log.error("load jar file failed, jarPath={}", jarPath, e);
            throw e;
        }
    }

    private ClassLoader findExtClassLoader(ClassLoader currentClassLoader) {
        ClassLoader p = currentClassLoader, l = null;
        do {
            if (p.getClass().getCanonicalName().endsWith(".ExtClassLoader")) {
                break;
            }
        } while (Objects.nonNull(p = (l = p).getParent()));
        return p;
    }

    @Override
    public int getOrder() {
        return PipelineOrderEnum.FIND_JAR_FILE_PIPELINE.getCode();
    }

}