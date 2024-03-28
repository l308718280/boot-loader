package com.soulw.boot.loader.domain.loader;

/**
 * Created by SoulW on 2024/3/28.
 *
 * @author SoulW
 */
public interface Pipeline extends PipelineSupport {

    /**
     * 执行pipeline
     *
     * @param context 上下文
     */
    void chain(Context context) throws Exception;

}
