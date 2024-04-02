package com.soulw.boot.loader.domain.loader.pipeline;

import com.alibaba.fastjson.JSON;
import com.google.common.collect.Lists;
import com.soulw.boot.loader.domain.loader.BasePipeline;
import com.soulw.boot.loader.domain.loader.Context;
import com.soulw.boot.loader.domain.loader.FatJarConfig;
import com.soulw.boot.loader.domain.loader.Pipeline;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;
import org.springframework.util.CollectionUtils;

import java.net.URISyntaxException;
import java.net.URL;
import java.util.Arrays;
import java.util.List;
import java.util.Objects;

/**
 * Created by SoulW on 2024/3/28.
 *
 * @author SoulW
 * @since 2024/3/28 10:05
 */
@Slf4j
@Component
public class WrapArgsPipeline extends BasePipeline {

    @Override
    protected void doChain(Context context, Pipeline next) throws Exception {
        String[] args = context.getRequestArgs();
        FatJarConfig config = context.getConfig();

        context.setFinalArgs(wrapArgs(args, config));
        log.info("WrapArgsPipeline#doChain success, requestArgs={}", JSON.toJSONString(context.getFinalArgs()));
        next.chain(context);
    }

    /**
     * 将参数进行包装处理，添加额外的参数，并将结果以字符串数组形式返回
     *
     * @param args   要进行包装处理的参数数组
     * @param config FatJarConfig
     * @return 包装处理后的参数数组
     */
    public String[] wrapArgs(String[] args, FatJarConfig config) throws URISyntaxException {
        List<String> newArgs = Lists.newArrayList();
        if (Objects.nonNull(args)) {
            newArgs.addAll(Arrays.asList(args));
        }
        if (Objects.nonNull(config.getPort())) {
            newArgs.add("--server.port=" + config.getPort());
        }
        if (!CollectionUtils.isEmpty(config.getAppendArgs())) {
            newArgs.addAll(config.getAppendArgs());
        }
        appendJmxConfigs(newArgs, config);
        return newArgs.toArray(new String[0]);
    }

    private void appendJmxConfigs(List<String> newArgs, FatJarConfig config) throws URISyntaxException {
        String uniqueName = config.getUniqueName();
        if (!newArgs.contains("--spring.application.name")) {
            newArgs.add("--spring.application.name=" + uniqueName);
        }
        if (!newArgs.contains("--spring.application.admin.jmx-name")) {
            newArgs.add("--spring.application.admin.jmx-name=org.springframework.boot:type=Admin,name=" + uniqueName);
        }
        if (!newArgs.contains("--spring.liveBeansView.mbeanDomain")) {
            newArgs.add("--spring.liveBeansView.mbeanDomain=" + uniqueName);
        }
        if (!newArgs.contains("--spring.config.location")) {
            String artifactId = config.getMavenVo().getArtifactId();
            URL classPath = getClass().getClassLoader().getResource("");
            String classPathPrefix = "optional:file:" + classPath.toURI().getPath();
            if (!classPathPrefix.endsWith("/")) {
                classPathPrefix += "/";
            }

            String directPath = classPathPrefix + artifactId + "/";
            newArgs.add("--spring.config.location=" + directPath);
        }
    }

    @Override
    public int getOrder() {
        return PipelineOrderEnum.WRAP_ARGS_PIPELINE.getCode();
    }
}
