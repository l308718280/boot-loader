package com.soulw.boot.loader.domain.loader.pipeline;

import lombok.AllArgsConstructor;
import lombok.Getter;

/**
 * Created by SoulW on 2024/4/1.
 *
 * @author SoulW
 */
@Getter
@AllArgsConstructor
public enum PipelineOrderEnum {
    VALIDATE_CONFIG_PIPELINE(100),
    FIND_JAR_FILE_PIPELINE(200),
    INSTALL_BOOT_LOADER_PIPELINE(300),
    WRAP_ARGS_PIPELINE(400),
    URL_STREAM_PIPELINE(500),
    PORT_PIPELINE(600),
    BOOT_RUN_PIPELINE(10000);

    private final int code;
}
