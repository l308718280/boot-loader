package com.soulw.boot.loader.domain.loader.exception;

/**
 * Created by SoulW on 2024/3/28.
 *
 * @author SoulW
 * @since 2024/3/28 15:56
 */
public class BootException extends RuntimeException {

    public BootException() {
    }

    public BootException(String message) {
        super(message);
    }

    public BootException(String message, Throwable cause) {
        super(message, cause);
    }

    public BootException(Throwable cause) {
        super(cause);
    }

    public BootException(String message, Throwable cause, boolean enableSuppression, boolean writableStackTrace) {
        super(message, cause, enableSuppression, writableStackTrace);
    }
}
