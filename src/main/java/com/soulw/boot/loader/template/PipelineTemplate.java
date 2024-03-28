package com.soulw.boot.loader.template;

import com.google.common.collect.Lists;
import com.soulw.boot.loader.config.BootLoaderConfig;
import com.soulw.boot.loader.domain.loader.Context;
import com.soulw.boot.loader.domain.loader.FatJarConfig;
import com.soulw.boot.loader.domain.loader.Pipeline;
import com.soulw.boot.loader.domain.loader.exception.BootException;
import com.soulw.boot.loader.domain.loader.exception.ContainerException;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.collections4.CollectionUtils;
import org.apache.commons.collections4.ListUtils;
import org.apache.commons.collections4.MapUtils;
import org.springframework.context.ApplicationContext;
import org.springframework.stereotype.Component;

import javax.annotation.PostConstruct;
import javax.annotation.Resource;
import java.lang.reflect.InvocationTargetException;
import java.util.Comparator;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

/**
 * Created by SoulW on 2024/3/28.
 *
 * @author SoulW
 * @since 2024/3/28 10:18
 */
@Component
@Slf4j
public class PipelineTemplate {
    private static final Pipeline EMPTY = new EmptyPipeline();
    @Resource
    private BootLoaderConfig bootLoaderConfig;
    @Resource
    private ApplicationContext applicationContext;
    private final List<Context> contexts = Lists.newCopyOnWriteArrayList();
    private Pipeline pipeline;

    @PostConstruct
    public void init() throws Exception {
        this.pipeline = getPipeline();
    }

    /**
     * 执行给定的上下文对象的管道链
     *
     * @param context 上下文对象
     * @throws Exception 可能抛出异常
     */
    public void execute(Context context) throws Exception {
        pipeline.chain(context);
    }

    /**
     * 执行默认操作，根据给定参数执行
     *
     * @param args 入参参数描述
     * @throws Exception 异常描述
     */
    public void executeByDefault(String[] args) throws Exception {
        List<FatJarConfig> fatJars = bootLoaderConfig.getFatJars();
        if (CollectionUtils.isEmpty(fatJars)) {
            throw new BootException("fatJars no config!!!");
        }

        printStatus();

        for (FatJarConfig fatJar : fatJars) {
            try {
                // 构建上下文
                Context context = new Context();
                context.setConfig(fatJar);
                context.setRequestArgs(args);
                // 执行链条
                pipeline.chain(context);
                // 记录上下文
                contexts.add(context);
                // 打印执行状态
                printStatus();
            } catch (Exception e) {
                swallowOrPrintException(e, fatJar);
            }
        }
    }

    private void swallowOrPrintException(Exception ex, FatJarConfig fatJar) throws Exception {
        if (ex instanceof ContainerException) {
            Throwable e = ex.getCause();
            while (e instanceof InvocationTargetException) {
                e = e.getCause();
            }

            if (e instanceof ContainerException) {
                throw (ContainerException) e;
            } else {
                throw new BootException("启动Jar包失败:【" + fatJar.getMavenVo().toMavenLocation() + ".jar】", e);
            }
        } else {
            throw ex;
        }
    }

    private void printStatus() {
        StringBuilder startupDesc = new StringBuilder();
        List<FatJarConfig> allJars = Lists.newArrayList();
        allJars.addAll(bootLoaderConfig.getFatJars());

        startupDesc.append("\n");
        startupDesc.append("===>>> 启动进度: [").append(contexts.size()).append("/").append(allJars.size()).append("]").append("\n");

        for (Context context : contexts) {
            startupDesc.append("===>>> [").append(context.getConfig().getMavenVo().toMavenLocation()).append(".jar] 成功").append("\n");
            allJars.remove(context.getConfig());
        }

        for (int i = 0; i < allJars.size(); i++) {
            FatJarConfig jar = allJars.get(i);
            startupDesc.append("===>>> [").append(jar.getMavenVo().toMavenLocation()).append(".jar] 未启动");
            if (i != allJars.size() - 1) {
                startupDesc.append("\n");
            }
        }

        if (log.isInfoEnabled()) {
            log.info(startupDesc.toString());
        }
    }

    private Pipeline getPipeline() {
        Map<String, Pipeline> pipelines = MapUtils.emptyIfNull(applicationContext.getBeansOfType(Pipeline.class));
        if (pipelines.isEmpty()) {
            return EMPTY;
        }

        List<String> deprecates = ListUtils.emptyIfNull(bootLoaderConfig.getDeprecatePiepelineClassNames());

        List<Pipeline> sortedPipelines = pipelines.values().stream()
                .filter(e -> {
                    String className = e.getClass().getCanonicalName();
                    return !deprecates.contains(className);
                })
                .sorted(Comparator.comparingInt(Pipeline::getOrder))
                .collect(Collectors.toList());

        Pipeline first = sortedPipelines.get(0), prev = first, now;

        for (int i = 1; i < sortedPipelines.size(); i++) {
            now = sortedPipelines.get(i);
            prev.setNext(now);

            prev = now;
        }

        return first;
    }

    /**
     * 空pipeline
     *
     * @author Soulw
     */
    public static class EmptyPipeline implements Pipeline {
        @Override
        public void chain(Context context) {
            throw new BootException("EmptyPipeline not allowed execute");
        }

        @Override
        public void setNext(Pipeline next) {
        }

        @Override
        public int getOrder() {
            return 0;
        }
    }


}
