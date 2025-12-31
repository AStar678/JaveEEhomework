package com.group.common.exception;

import com.group.common.dto.Result;
import lombok.extern.slf4j.Slf4j;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;

import javax.servlet.http.HttpServletRequest;

@Slf4j
@RestControllerAdvice
public class GlobalExceptionHandler {

    @ExceptionHandler(Exception.class)
    public Result<String> handleException(Exception e, HttpServletRequest request) {
        // 记录错误日志，因为配置了 MDC，这里的日志会自动带上 traceId
        log.error("Request URL: {}, Exception: ", request.getRequestURI(), e);

        // 返回友好的错误提示
        return Result.error(500, "系统内部错误: " + e.getMessage());
    }

    // 你可以扩展更多的异常处理，比如自定义业务异常
    // @ExceptionHandler(BusinessException.class) ...
}