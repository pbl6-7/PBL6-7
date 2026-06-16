package com.campus.core.common;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.validation.BindException;
import org.springframework.validation.FieldError;
import org.springframework.web.bind.MethodArgumentNotValidException;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.ResponseStatus;
import org.springframework.web.bind.annotation.RestControllerAdvice;
import org.springframework.web.context.request.WebRequest;
import org.springframework.web.method.annotation.MethodArgumentTypeMismatchException;

import javax.servlet.http.HttpServletRequest;
import javax.validation.ConstraintViolationException;
import java.util.HashMap;
import java.util.Map;
import java.util.UUID;
import java.util.stream.Collectors;

/**
 * 全局异常处理器
 * 统一处理所有异常，返回标准格式的错误响应
 */
@RestControllerAdvice
public class GlobalExceptionHandler {

    private static final Logger logger = LoggerFactory.getLogger(GlobalExceptionHandler.class);

    /**
     * 生成请求ID
     * 用于追踪和排查问题
     *
     * @return 请求ID
     */
    private String generateRequestId() {
        return UUID.randomUUID().toString().replace("-", "").substring(0, 16);
    }

    /**
     * 构建请求上下文信息
     * 包含用户ID、请求路径、请求方法等
     *
     * @param request HTTP请求
     * @return 请求上下文信息
     */
    private Map<String, Object> buildRequestContext(HttpServletRequest request) {
        Map<String, Object> context = new HashMap<>();
        context.put("path", request.getRequestURI());
        context.put("method", request.getMethod());
        context.put("queryString", request.getQueryString());
        
        // 获取用户ID（从请求属性中获取，由拦截器设置）
        Object userId = request.getAttribute("currentUserId");
        if (userId != null) {
            context.put("userId", userId);
        }
        
        // 获取客户端IP
        String ip = request.getHeader("X-Forwarded-For");
        if (ip == null || ip.isEmpty()) {
            ip = request.getRemoteAddr();
        }
        context.put("clientIp", ip);
        
        return context;
    }

    /**
     * 构建错误详情
     *
     * @param exception 异常对象
     * @param request   HTTP请求
     * @return 错误详情
     */
    private Map<String, Object> buildErrorDetails(Exception exception, HttpServletRequest request) {
        Map<String, Object> details = new HashMap<>();
        details.put("request", buildRequestContext(request));
        
        // 根据异常类型添加特定详情
        if (exception instanceof ResourceNotFoundException) {
            ResourceNotFoundException e = (ResourceNotFoundException) exception;
            if (e.getResourceType() != null) {
                details.put("resourceType", e.getResourceType());
            }
            if (e.getResourceId() != null) {
                details.put("resourceId", e.getResourceId());
            }
        } else if (exception instanceof DuplicateResourceException) {
            DuplicateResourceException e = (DuplicateResourceException) exception;
            if (e.getConflictField() != null) {
                details.put("conflictField", e.getConflictField());
            }
            if (e.getConflictValue() != null) {
                details.put("conflictValue", e.getConflictValue());
            }
        } else if (exception instanceof OperationNotAllowedException) {
            OperationNotAllowedException e = (OperationNotAllowedException) exception;
            if (e.getOperation() != null) {
                details.put("operation", e.getOperation());
            }
            if (e.getReason() != null) {
                details.put("reason", e.getReason());
            }
        } else if (exception instanceof ValidationException) {
            ValidationException e = (ValidationException) exception;
            if (e.getField() != null) {
                details.put("field", e.getField());
            }
            if (e.getRejectedValue() != null) {
                details.put("rejectedValue", e.getRejectedValue());
            }
            if (e.getErrors() != null) {
                details.put("validationErrors", e.getErrors());
            }
        } else if (exception instanceof RateLimitExceededException) {
            RateLimitExceededException e = (RateLimitExceededException) exception;
            details.put("limitType", e.getLimitType());
            details.put("limitKey", e.getLimitKey());
            details.put("retryAfterSeconds", e.getRetryAfterSeconds());
        }
        
        return details;
    }

    /**
     * 记录业务异常日志（警告级别）
     * 只记录关键信息，不记录完整堆栈
     *
     * @param exception 异常对象
     * @param requestId 请求ID
     * @param request   HTTP请求
     */
    private void logBusinessException(Exception exception, String requestId, HttpServletRequest request) {
        Map<String, Object> context = buildRequestContext(request);
        logger.warn("[{}] 业务异常 - 类型: {}, 消息: {}, 上下文: {}",
                requestId,
                exception.getClass().getSimpleName(),
                exception.getMessage(),
                context);
    }

    /**
     * 记录系统异常日志（错误级别）
     * 记录完整堆栈信息
     *
     * @param exception 异常对象
     * @param requestId 请求ID
     * @param request   HTTP请求
     */
    private void logSystemException(Exception exception, String requestId, HttpServletRequest request) {
        Map<String, Object> context = buildRequestContext(request);
        logger.error("[{}] 系统异常 - 类型: {}, 消息: {}, 上下文: {}",
                requestId,
                exception.getClass().getSimpleName(),
                exception.getMessage(),
                context,
                exception);
    }

    // ==================== 认证授权异常处理 ====================

    /**
     * 处理认证异常
     */
    @ExceptionHandler(AuthenticationException.class)
    @ResponseStatus(HttpStatus.UNAUTHORIZED)
    public Result<Void> handleAuthenticationException(AuthenticationException e, HttpServletRequest request) {
        String requestId = generateRequestId();
        logBusinessException(e, requestId, request);
        
        Map<String, Object> details = buildErrorDetails(e, request);
        
        return Result.<Void>error(e.getCode(), e.getMessage())
                .requestId(requestId)
                .details(details);
    }

    /**
     * 处理授权异常
     */
    @ExceptionHandler(AuthorizationException.class)
    @ResponseStatus(HttpStatus.FORBIDDEN)
    public Result<Void> handleAuthorizationException(AuthorizationException e, HttpServletRequest request) {
        String requestId = generateRequestId();
        logBusinessException(e, requestId, request);
        
        Map<String, Object> details = buildErrorDetails(e, request);
        
        return Result.<Void>error(e.getCode(), e.getMessage())
                .requestId(requestId)
                .details(details);
    }

    // ==================== 资源异常处理 ====================

    /**
     * 处理资源未找到异常
     */
    @ExceptionHandler(ResourceNotFoundException.class)
    @ResponseStatus(HttpStatus.NOT_FOUND)
    public Result<Void> handleResourceNotFoundException(ResourceNotFoundException e, HttpServletRequest request) {
        String requestId = generateRequestId();
        logBusinessException(e, requestId, request);
        
        Map<String, Object> details = buildErrorDetails(e, request);
        
        return Result.<Void>error(e.getCode(), e.getMessage())
                .requestId(requestId)
                .details(details);
    }

    /**
     * 处理资源重复异常
     */
    @ExceptionHandler(DuplicateResourceException.class)
    @ResponseStatus(HttpStatus.CONFLICT)
    public Result<Void> handleDuplicateResourceException(DuplicateResourceException e, HttpServletRequest request) {
        String requestId = generateRequestId();
        logBusinessException(e, requestId, request);
        
        Map<String, Object> details = buildErrorDetails(e, request);
        
        return Result.<Void>error(e.getCode(), e.getMessage())
                .requestId(requestId)
                .details(details);
    }

    // ==================== 操作异常处理 ====================

    /**
     * 处理操作不允许异常
     */
    @ExceptionHandler(OperationNotAllowedException.class)
    @ResponseStatus(HttpStatus.BAD_REQUEST)
    public Result<Void> handleOperationNotAllowedException(OperationNotAllowedException e, HttpServletRequest request) {
        String requestId = generateRequestId();
        logBusinessException(e, requestId, request);
        
        Map<String, Object> details = buildErrorDetails(e, request);
        
        return Result.<Void>error(e.getCode(), e.getMessage())
                .requestId(requestId)
                .details(details);
    }

    // ==================== 验证异常处理 ====================

    /**
     * 处理验证异常
     */
    @ExceptionHandler(ValidationException.class)
    @ResponseStatus(HttpStatus.BAD_REQUEST)
    public Result<Void> handleValidationException(ValidationException e, HttpServletRequest request) {
        String requestId = generateRequestId();
        logBusinessException(e, requestId, request);
        
        Map<String, Object> details = buildErrorDetails(e, request);
        
        return Result.<Void>error(e.getCode(), e.getMessage())
                .requestId(requestId)
                .details(details);
    }

    /**
     * 处理参数校验异常（请求体）
     */
    @ExceptionHandler(MethodArgumentNotValidException.class)
    @ResponseStatus(HttpStatus.BAD_REQUEST)
    public Result<Void> handleMethodArgumentNotValidException(MethodArgumentNotValidException e, HttpServletRequest request) {
        String requestId = generateRequestId();
        
        Map<String, String> fieldErrors = e.getBindingResult().getFieldErrors()
                .stream()
                .collect(Collectors.toMap(
                        FieldError::getField,
                        fieldError -> fieldError.getDefaultMessage() != null ? fieldError.getDefaultMessage() : "验证失败",
                        (existing, replacement) -> existing + "; " + replacement
                ));
        
        String message = fieldErrors.values().stream().collect(Collectors.joining(", "));
        
        logger.warn("[{}] 参数校验异常 - 消息: {}, 字段错误: {}, 上下文: {}",
                requestId,
                message,
                fieldErrors,
                buildRequestContext(request));
        
        Map<String, Object> details = new HashMap<>();
        details.put("request", buildRequestContext(request));
        details.put("validationErrors", fieldErrors);
        
        return Result.<Void>error(ResultCode.VALIDATION_ERROR, message)
                .requestId(requestId)
                .details(details);
    }

    /**
     * 处理参数校验异常（表单绑定）
     */
    @ExceptionHandler(BindException.class)
    @ResponseStatus(HttpStatus.BAD_REQUEST)
    public Result<Void> handleBindException(BindException e, HttpServletRequest request) {
        String requestId = generateRequestId();
        
        Map<String, String> fieldErrors = e.getBindingResult().getFieldErrors()
                .stream()
                .collect(Collectors.toMap(
                        FieldError::getField,
                        fieldError -> fieldError.getDefaultMessage() != null ? fieldError.getDefaultMessage() : "验证失败",
                        (existing, replacement) -> existing + "; " + replacement
                ));
        
        String message = fieldErrors.values().stream().collect(Collectors.joining(", "));
        
        logger.warn("[{}] 绑定异常 - 消息: {}, 字段错误: {}, 上下文: {}",
                requestId,
                message,
                fieldErrors,
                buildRequestContext(request));
        
        Map<String, Object> details = new HashMap<>();
        details.put("request", buildRequestContext(request));
        details.put("validationErrors", fieldErrors);
        
        return Result.<Void>error(ResultCode.VALIDATION_ERROR, message)
                .requestId(requestId)
                .details(details);
    }

    /**
     * 处理约束违反异常（单个参数验证）
     */
    @ExceptionHandler(ConstraintViolationException.class)
    @ResponseStatus(HttpStatus.BAD_REQUEST)
    public Result<Void> handleConstraintViolationException(ConstraintViolationException e, HttpServletRequest request) {
        String requestId = generateRequestId();
        
        String message = e.getConstraintViolations()
                .stream()
                .map(violation -> violation.getMessage())
                .collect(Collectors.joining(", "));
        
        logger.warn("[{}] 约束违反异常 - 消息: {}, 上下文: {}",
                requestId,
                message,
                buildRequestContext(request));
        
        Map<String, Object> details = new HashMap<>();
        details.put("request", buildRequestContext(request));
        
        return Result.<Void>error(ResultCode.VALIDATION_ERROR, message)
                .requestId(requestId)
                .details(details);
    }

    /**
     * 处理参数类型不匹配异常
     */
    @ExceptionHandler(MethodArgumentTypeMismatchException.class)
    @ResponseStatus(HttpStatus.BAD_REQUEST)
    public Result<Void> handleMethodArgumentTypeMismatchException(MethodArgumentTypeMismatchException e, HttpServletRequest request) {
        String requestId = generateRequestId();
        
        String message = String.format("参数'%s'类型不正确，期望类型: %s",
                e.getName(),
                e.getRequiredType() != null ? e.getRequiredType().getSimpleName() : "未知");
        
        logger.warn("[{}] 参数类型不匹配异常 - 消息: {}, 上下文: {}",
                requestId,
                message,
                buildRequestContext(request));
        
        Map<String, Object> details = new HashMap<>();
        details.put("request", buildRequestContext(request));
        details.put("parameterName", e.getName());
        details.put("requiredType", e.getRequiredType() != null ? e.getRequiredType().getSimpleName() : null);
        details.put("rejectedValue", e.getValue());
        
        return Result.<Void>error(ResultCode.BAD_REQUEST, message)
                .requestId(requestId)
                .details(details);
    }

    // ==================== 限流异常处理 ====================

    /**
     * 处理限流异常
     */
    @ExceptionHandler(RateLimitExceededException.class)
    @ResponseStatus(HttpStatus.TOO_MANY_REQUESTS)
    public Result<Void> handleRateLimitExceededException(RateLimitExceededException e, HttpServletRequest request) {
        String requestId = generateRequestId();
        logBusinessException(e, requestId, request);
        
        Map<String, Object> details = buildErrorDetails(e, request);
        
        String message = String.format("%s，请在%d秒后重试", e.getMessage(), e.getRetryAfterSeconds());
        
        return Result.<Void>error(ResultCode.RATE_LIMIT_EXCEEDED.getCode(), message)
                .requestId(requestId)
                .details(details);
    }

    // ==================== 业务异常处理 ====================

    /**
     * 处理业务异常
     * 根据错误码映射到正确的HTTP状态码
     */
    @ExceptionHandler(BusinessException.class)
    public ResponseEntity<Result<Void>> handleBusinessException(BusinessException e, HttpServletRequest request) {
        String requestId = generateRequestId();
        logBusinessException(e, requestId, request);

        Map<String, Object> details = new HashMap<>();
        details.put("request", buildRequestContext(request));

        // 根据错误码映射HTTP状态码
        HttpStatus httpStatus = mapBusinessCodeToHttpStatus(e.getCode());

        return ResponseEntity.status(httpStatus)
                .body(Result.<Void>error(e.getCode(), e.getMessage())
                        .requestId(requestId)
                        .details(details));
    }

    /**
     * 根据业务错误码映射HTTP状态码
     * @param code 业务错误码
     * @return 对应的HTTP状态码
     */
    private HttpStatus mapBusinessCodeToHttpStatus(Integer code) {
        if (code == null) {
            return HttpStatus.BAD_REQUEST;
        }
        // 权限相关错误码(1xxx)返回403
        if (code >= 1101 && code <= 1199) {
            return HttpStatus.FORBIDDEN;
        }
        // 认证相关错误码(1xxx)返回401
        if (code >= 1001 && code <= 1099) {
            return HttpStatus.UNAUTHORIZED;
        }
        // NOT_FOUND相关错误码(2001-2099, 4010-4011)返回404
        if ((code >= 2001 && code <= 2099) || code == 4010 || code == 4011) {
            return HttpStatus.NOT_FOUND;
        }
        // 权限不足专用错误码(4013-4014)返回403
        if (code == 4013 || code == 4014) {
            return HttpStatus.FORBIDDEN;
        }
        // 验证错误(422, 6xxx)返回422
        if (code == 422 || (code >= 6001 && code <= 6999)) {
            return HttpStatus.UNPROCESSABLE_ENTITY;
        }
        // 限流错误(7xxx)返回429
        if (code >= 7001 && code <= 7999) {
            return HttpStatus.TOO_MANY_REQUESTS;
        }
        // 默认返回400
        return HttpStatus.BAD_REQUEST;
    }

    // ==================== 系统异常处理 ====================

    /**
     * 处理非法参数异常
     */
    @ExceptionHandler(IllegalArgumentException.class)
    @ResponseStatus(HttpStatus.BAD_REQUEST)
    public Result<Void> handleIllegalArgumentException(IllegalArgumentException e, HttpServletRequest request) {
        String requestId = generateRequestId();
        logBusinessException(e, requestId, request);
        
        Map<String, Object> details = new HashMap<>();
        details.put("request", buildRequestContext(request));
        
        return Result.<Void>error(ResultCode.BAD_REQUEST, e.getMessage())
                .requestId(requestId)
                .details(details);
    }

    /**
     * 处理空指针异常
     */
    @ExceptionHandler(NullPointerException.class)
    @ResponseStatus(HttpStatus.INTERNAL_SERVER_ERROR)
    public Result<Void> handleNullPointerException(NullPointerException e, HttpServletRequest request) {
        String requestId = generateRequestId();
        logSystemException(e, requestId, request);
        
        return Result.<Void>error(ResultCode.INTERNAL_SERVER_ERROR, "系统内部错误")
                .requestId(requestId);
    }

    /**
     * 处理未知异常
     */
    @ExceptionHandler(Exception.class)
    @ResponseStatus(HttpStatus.INTERNAL_SERVER_ERROR)
    public Result<Void> handleException(Exception e, HttpServletRequest request) {
        String requestId = generateRequestId();
        logSystemException(e, requestId, request);
        
        return Result.<Void>error(ResultCode.INTERNAL_SERVER_ERROR, "系统内部错误")
                .requestId(requestId);
    }
}