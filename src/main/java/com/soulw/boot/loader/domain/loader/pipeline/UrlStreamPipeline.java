package com.soulw.boot.loader.domain.loader.pipeline;

import com.google.common.collect.Maps;
import com.soulw.boot.loader.domain.loader.BasePipeline;
import com.soulw.boot.loader.domain.loader.Context;
import com.soulw.boot.loader.domain.loader.Pipeline;
import com.soulw.boot.loader.domain.loader.exception.BootException;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;

import java.lang.reflect.Field;
import java.net.URL;
import java.net.URLStreamHandler;
import java.net.URLStreamHandlerFactory;
import java.util.Hashtable;
import java.util.Map;
import java.util.Objects;

/**
 * Created by SoulW on 2024/3/27.
 *
 * @author SoulW
 * @since 2024/3/27 18:28
 */
@Slf4j
@Component
public class UrlStreamPipeline extends BasePipeline {

    private static final Field handlersField;
    private static final Field factoryField;

    @Override
    @SuppressWarnings("unchecked")
    protected void doChain(Context context, Pipeline next) throws Exception {
        // step1. 记录
        URLStreamHandlerFactory factory = (URLStreamHandlerFactory) factoryField.get(null);
        Hashtable<String, URLStreamHandler> handlers = (Hashtable<String, URLStreamHandler>) handlersField.get(null);
        Map<String, URLStreamHandler> snapshot = Maps.newHashMap();
        snapshot.putAll(handlers);

        // step2. 清理
        clear(handlers);

        // step3. 执行链条
        next.chain(context);

        // step4. 重写
        rewrite(factory, handlers, snapshot);
    }

    /**
     * 清除工厂字段和处理程序列表
     */
    public void clear(Hashtable<String, URLStreamHandler> handlers) {
        try {
            factoryField.set(null, null);
            if (Objects.nonNull(handlers)) {
                handlers.clear();
            }
        } catch (Exception e) {
            log.error("clear failed", e);
            throw new BootException("clear failed", e);
        }
    }

    /**
     * 重写方法
     */
    public void rewrite(URLStreamHandlerFactory factory, Hashtable<String, URLStreamHandler> handlers, Map<String, URLStreamHandler> snapshot) {
        try {
            if (Objects.nonNull(factory)) {
                factoryField.set(null, factory);
            }
            handlers.putAll(snapshot);
        } catch (Exception e) {
            log.error("rewrite failed", e);
            throw new BootException("rewrite failed", e);
        }
    }

    static {
        try {
            handlersField = URL.class.getDeclaredField("handlers");
            handlersField.setAccessible(true);

            factoryField = URL.class.getDeclaredField("factory");
            factoryField.setAccessible(true);
        } catch (Exception e) {
            log.error("init UrlStreamRewriter failed", e);
            throw new BootException("init failed", e);
        }
    }

    @Override
    public int getOrder() {
        return PipelineOrderEnum.URL_STREAM_PIPELINE.getCode();
    }
}
