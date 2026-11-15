package com.poolwise.exception;

/** Base custom exception for all POOLWISE application errors. */
public class PoolwiseException extends Exception {
    private final String errorCode;

    public PoolwiseException(String errorCode, String message) {
        super(message);
        this.errorCode = errorCode;
    }

    public PoolwiseException(String errorCode, String message, Throwable cause) {
        super(message, cause);
        this.errorCode = errorCode;
    }

    public String getErrorCode() { return errorCode; }

    @Override
    public String toString() { return "[" + errorCode + "] " + getMessage(); }
}
