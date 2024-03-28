package com.soulw.boot.loader.domain.loader.exception;

/**
 * Created by SoulW on 2024/3/28.
 *
 * @author SoulW
 * @since 2024/3/28 16:00
 */
public class ContainerException extends RuntimeException {

    public ContainerException() {
    }

    public ContainerException(String message) {
        super(message);
    }

    public ContainerException(String message, Throwable cause) {
        super(message, cause);
    }

    public ContainerException(Throwable cause) {
        super(cause);
    }

    public ContainerException(String message, Throwable cause, boolean enableSuppression, boolean writableStackTrace) {
        super(message, cause, enableSuppression, writableStackTrace);
    }
}
