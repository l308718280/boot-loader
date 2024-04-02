package com.soulw.boot.loader.domain.loader.finder;

import com.google.common.base.Preconditions;
import com.google.common.collect.Lists;
import com.google.common.collect.Sets;
import com.soulw.boot.loader.domain.loader.MavenVO;
import lombok.Value;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.lang3.StringUtils;

import javax.annotation.Nullable;
import javax.servlet.ServletContext;
import java.io.File;
import java.io.IOException;
import java.net.MalformedURLException;
import java.net.URL;
import java.nio.file.FileVisitResult;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.nio.file.SimpleFileVisitor;
import java.nio.file.attribute.BasicFileAttributes;
import java.util.List;
import java.util.Objects;

/**
 * Created by SoulW on 2024/3/27.
 *
 * @author SoulW
 * @since 2024/3/27 17:27
 */
@Value
@Slf4j
public class JarFinder {
    private static final String DYNAMIC_CLASS_ENV = "deploy.dynamic.config.dir";
    private static final String TOMCAT_LIB_PATH = "/WEB-INF/lib";
    private static final String CLASS_PATH_SPLIT = ";";
    private static final String LIB_DIR = "/lib";
    private static final int MAX_DEPTH = 5;
    MavenVO mavenVO;
    String mavenLocation;
    String mavenPath;
    String mavenFileName;
    ServletContext servletContext;

    public JarFinder(MavenVO mavenVO, ServletContext servletContext) {
        this.mavenVO = mavenVO;
        this.mavenLocation = mavenVO.toMavenLocation();
        this.mavenPath = mavenVO.toMavenPath();
        this.mavenFileName = mavenVO.toJarFileName();
        this.servletContext = servletContext;
    }

    @Nullable
    public String findJarPath() throws MalformedURLException {
        String jarPath;
        return Objects.nonNull(jarPath = findJarPathBySysPath()) ||
                Objects.nonNull(jarPath = findByTomcat()) ||
                Objects.nonNull(jarPath = findByMvn()) ||
                Objects.nonNull(jarPath = findByEnv()) ? jarPath : null;
    }

    private String findByEnv() {
        String env = System.getProperty(DYNAMIC_CLASS_ENV);
        if (StringUtils.isBlank(env)) {
            return null;
        }

        Path path = Paths.get(env);
        if (!path.toFile().exists()) {
            return null;
        }

        File libDir = new File(path.getParent().toAbsolutePath() + LIB_DIR);
        if (!libDir.exists()) {
            return null;
        }
        return findJarFileInDirectory(libDir);
    }

    private String findByTomcat() throws MalformedURLException {
        URL lib = servletContext.getResource(TOMCAT_LIB_PATH);
        if (Objects.isNull(lib)) {
            return null;
        }

        File libFile = new File(lib.getFile());
        if (!libFile.exists() || !libFile.isDirectory()) {
            return null;
        }

        return findJarFileInDirectory(libFile);
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
            Preconditions.checkNotNull(directory, "directory is null");

            List<Path> findPath = Lists.newArrayList();
            Files.walkFileTree(Paths.get(directory.getPath()), Sets.newHashSet(), MAX_DEPTH, new SimpleFileVisitor<Path>() {
                @Override
                public FileVisitResult visitFile(Path file, BasicFileAttributes attrs) {
                    boolean matchJar = matchJar(file.toString());
                    if (matchJar) {
                        findPath.add(file);
                    }
                    return FileVisitResult.CONTINUE;
                }
            });
            return findPath.stream()
                    .sorted((f, n) -> -1 * Long.compare(f.toFile().lastModified(), n.toFile().lastModified()))
                    .map(Path::toString)
                    .findFirst().orElse(null);
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
