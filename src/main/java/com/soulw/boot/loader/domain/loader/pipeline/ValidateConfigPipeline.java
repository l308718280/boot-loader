package com.soulw.boot.loader.domain.loader.pipeline;

import com.alibaba.fastjson.JSON;
import com.alibaba.fastjson.serializer.SerializerFeature;
import com.google.common.base.Preconditions;
import com.soulw.boot.loader.domain.loader.BasePipeline;
import com.soulw.boot.loader.domain.loader.Context;
import com.soulw.boot.loader.domain.loader.FatJarConfig;
import com.soulw.boot.loader.domain.loader.MavenVO;
import com.soulw.boot.loader.domain.loader.Pipeline;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;

/**
 * Created by SoulW on 2024/3/28.
 *
 * @author SoulW
 * @since 2024/3/28 9:53
 */
@Slf4j
@Component
public class ValidateConfigPipeline extends BasePipeline {

    @Override
    protected void doChain(Context context, Pipeline next) throws Exception {
        FatJarConfig config = context.getConfig();
        Preconditions.checkNotNull(config, "FatJarConfig is null");
        Preconditions.checkNotNull(config.getMavenVo(), "FatJarConfig.mavenVO is null");

        MavenVO mavenVO = config.getMavenVo();
        Preconditions.checkNotNull(mavenVO.getGroupId(), "groupId is null");
        Preconditions.checkNotNull(mavenVO.getVersion(), "version is null");
        Preconditions.checkNotNull(mavenVO.getArtifactId(), "artifactId is null");

        log.info("ValidateConfigPipeline#doChain success, uniqueName={}, config={}", config.getUniqueName(), JSON.toJSONString(config, SerializerFeature.PrettyFormat));
        next.chain(context);
    }

    @Override
    public int getOrder() {
        return 10;
    }
}
