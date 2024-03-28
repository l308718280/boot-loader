package com.soulw.boot.loader.domain.loader.pipeline;

import com.google.common.base.Preconditions;
import com.google.common.collect.Lists;
import com.google.common.collect.Sets;
import com.soulw.boot.loader.domain.loader.BasePipeline;
import com.soulw.boot.loader.domain.loader.Context;
import com.soulw.boot.loader.domain.loader.MavenVO;
import com.soulw.boot.loader.domain.loader.Pipeline;
import lombok.Value;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.lang3.StringUtils;
import org.springframework.stereotype.Component;
import org.springframework.util.CollectionUtils;

import javax.annotation.Nonnull;
import java.io.File;
import java.io.IOException;
import java.net.MalformedURLException;
import java.net.URL;
import java.net.URLClassLoader;
import java.nio.file.FileVisitResult;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.nio.file.SimpleFileVisitor;
import java.nio.file.attribute.BasicFileAttributes;
import java.util.List;
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

    @Override
    protected void doChain(Context context, Pipeline next) throws Exception {
        ClassLoader currentClassLoader = getClass().getClassLoader();
        ClassLoader extClassLoader = findExtClassLoader(currentClassLoader);
        Preconditions.checkNotNull(extClassLoader, "extClassLoader no found!!!");

        String jarPath = new JarFinder(context.getConfig().getMavenVo())
                .findJarPath();
        Preconditions.checkNotNull(jarPath, "jarPath no found!");

        context.setJarPath(jarPath);
        context.setAppClassLoader(initAppClassLoader(jarPath, extClassLoader));

        log.info("InitPipeline#doChain startup success");
        next.chain(context);
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
        return 20;
    }

    /**
     * Created by SoulW on 2024/3/27.
     *
     * @author SoulW
     * @since 2024/3/27 17:27
     */
    @Value
    @Slf4j
    public static class JarFinder {
        private static final String CLASS_PATH_SPLIT = ";";
        private static final int MAX_DEPTH = 5;
        MavenVO mavenVO;
        String mavenLocation;
        String mavenPath;
        String mavenFileName;

        public JarFinder(MavenVO mavenVO) {
            this.mavenVO = mavenVO;
            this.mavenLocation = mavenVO.toMavenLocation();
            this.mavenPath = mavenVO.toMavenPath();
            this.mavenFileName = mavenVO.toJarFileName();
        }

        @Nonnull
        public String findJarPath() {
            String jarPath;
            jarPath = findJarPathBySysPath();
            if (StringUtils.isNotBlank(jarPath)) {
                return jarPath;
            }

            jarPath = findByMvn();
            if (StringUtils.isNotBlank(jarPath)) {
                return jarPath;
            }

            return jarPath;
        }

        private String findByMvn() {
            String userHome = System.getProperty("user.home");
            if (StringUtils.isBlank(userHome)) {
                return null;
            }
            String mavenPath = String.join(File.separator, userHome, ".m2", "repository", this.mavenPath);
            File dir = new File(mavenPath);
            if (!dir.exists()) {
                return null;
            }

            return findJarFileInDirectory(dir);
        }

        private String findJarPathBySysPath() {
            String classPath = System.getProperty("java.class.path");
            if (StringUtils.isBlank(classPath)) {
                return null;
            }

            for (String jarFile : classPath.split(CLASS_PATH_SPLIT)) {
                jarFile = StringUtils.trim(jarFile);
                if (StringUtils.isBlank(jarFile)) {
                    continue;
                }

                if (jarFile.endsWith(".jar")) {
                    if (matchJar(jarFile)) {
                        return jarFile;
                    }
                    continue;
                }

                File directory = new File(jarFile);
                if (directory.isDirectory()) {
                    String r = findJarFileInDirectory(directory);
                    if (Objects.nonNull(r)) {
                        return r;
                    }
                }
            }

            return null;
        }

        private String findJarFileInDirectory(File directory) {
            try {
                List<Path> findPath = Lists.newArrayList();
                Files.walkFileTree(Paths.get(directory.getPath()), Sets.newHashSet(), MAX_DEPTH, new SimpleFileVisitor<Path>() {
                    @Override
                    public FileVisitResult visitFile(Path file, BasicFileAttributes attrs) {
                        boolean matchJar = matchJar(file.toString());
                        if (matchJar) {
                            findPath.add(file);
                        }
                        return matchJar ? FileVisitResult.TERMINATE : FileVisitResult.CONTINUE;
                    }
                });
                return CollectionUtils.isEmpty(findPath) ? null : findPath.get(0).toString();
            } catch (IOException e) {
                log.error("load file tree failed", e);
                return null;
            }
        }

        private boolean matchJar(String jarFile) {
            if (!jarFile.endsWith(".jar")) {
                return false;
            }

            return jarFile.contains(mavenFileName);
        }

    }

}
