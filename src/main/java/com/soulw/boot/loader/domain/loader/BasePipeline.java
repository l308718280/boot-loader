package com.soulw.boot.loader.domain.loader;

import com.google.common.base.Preconditions;
import lombok.Getter;
import lombok.Setter;
import lombok.extern.slf4j.Slf4j;

/**
 * Created by SoulW on 2024/3/28.
 *
 * @author SoulW
 * @since 2024/3/28 9:47
 */
@Setter
@Getter
@Slf4j
public abstract class BasePipeline implements Pipeline {

    /**
     * pipeline 链条
     */
    private Pipeline next;

    @Override
    public void chain(Context context) throws Exception {
        Preconditions.checkNotNull(context, "context is null");
        try {
            // step1. 执行当前命令
            doChain(context, next);
        } catch (Exception e) {
            if (isPrintErrorLog()) {
                log.error("{}#doChain failed", getSubName(), e);
            }
            throw e;
        }
    }

    protected boolean isPrintErrorLog() {
        return false;
    }

    private String getSubName() {
        return getClass().getCanonicalName();
    }

    protected abstract void doChain(Context context, Pipeline next) throws Exception;
}
