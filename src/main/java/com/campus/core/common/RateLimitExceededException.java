package com.campus.core.common;

/**
 * 限流异常
 */
public class RateLimitExceededException extends BusinessException {
    
    private final int limit;
    private final int retryAfterSeconds;
    private final String limitType;
    private final String limitKey;

    public RateLimitExceededException(String message) {
        super(ResultCode.RATE_LIMIT_EXCEEDED, message);
        this.limit = 0;
        this.retryAfterSeconds = 0;
        this.limitType = null;
        this.limitKey = null;
    }
    
    public RateLimitExceededException(int limit, int retryAfterSeconds) {
        super(ResultCode.RATE_LIMIT_EXCEEDED, "Rate limit exceeded. Limit: " + limit + ", Retry after: " + retryAfterSeconds + " seconds");
        this.limit = limit;
        this.retryAfterSeconds = retryAfterSeconds;
        this.limitType = null;
        this.limitKey = null;
    }
    
    public RateLimitExceededException(int limit, int retryAfterSeconds, String message) {
        super(ResultCode.RATE_LIMIT_EXCEEDED, message);
        this.limit = limit;
        this.retryAfterSeconds = retryAfterSeconds;
        this.limitType = null;
        this.limitKey = null;
    }

    public RateLimitExceededException(String limitType, String limitKey, int retryAfterSeconds) {
        super(ResultCode.RATE_LIMIT_EXCEEDED, "Rate limit exceeded for " + limitType + ": " + limitKey);
        this.limit = 0;
        this.retryAfterSeconds = retryAfterSeconds;
        this.limitType = limitType;
        this.limitKey = limitKey;
    }

    public int getLimit() {
        return limit;
    }

    public int getRetryAfterSeconds() {
        return retryAfterSeconds;
    }

    public String getLimitType() {
        return limitType;
    }

    public String getLimitKey() {
        return limitKey;
    }
}
