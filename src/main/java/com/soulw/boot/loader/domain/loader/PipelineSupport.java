package com.soulw.boot.loader.domain.loader;

/**
 * Created by SoulW on 2024/3/28.
 *
 * @author SoulW
 */
public interface PipelineSupport {

    /**
     * 设置下一个管道
     *
     * @param next 下一个管道
     */
    void setNext(Pipeline next);

    /**
     * 获取订单数量
     *
     * @return 订单数量
     */
    int getOrder();
}
