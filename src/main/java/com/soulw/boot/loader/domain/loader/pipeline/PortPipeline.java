package com.soulw.boot.loader.domain.loader.pipeline;

import com.soulw.boot.loader.domain.loader.BasePipeline;
import com.soulw.boot.loader.domain.loader.Context;
import com.soulw.boot.loader.domain.loader.Pipeline;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;

import java.util.Objects;

/**
 * Created by SoulW on 2024/3/28.
 *
 * @author SoulW
 * @since 2024/3/28 11:19
 */
@Component
@Slf4j
public class PortPipeline extends BasePipeline {

    private static final String SERVER_PORT = "server.port";

    @Override
    protected void doChain(Context context, Pipeline next) throws Exception {
        Integer port = context.getConfig().getPort();
        if (Objects.isNull(port)) {
            next.chain(context);
        }

        String oldPort = System.getProperty(SERVER_PORT);

        System.setProperty(SERVER_PORT, String.valueOf(port));
        log.info("PortPipeline#doChain success");
        next.chain(context);
        if (Objects.isNull(oldPort)) {
            System.getProperties().remove(SERVER_PORT);
        } else {
            System.setProperty(SERVER_PORT, oldPort);
        }
    }

    @Override
    public int getOrder() {
        return 50;
    }
}
