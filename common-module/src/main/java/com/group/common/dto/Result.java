package com.group.common.dto;

import lombok.Data;
import lombok.NoArgsConstructor;
import org.slf4j.MDC;

import java.io.Serializable;

/**
 * 统一响应结构
 */
@Data
@NoArgsConstructor
public class Result<T> implements Serializable {
    private Integer code;       // 200 成功, 500 失败
    private String message;     // 提示信息
    private T data;            // 数据负载
    private String traceId;     // 返回 TraceId 方便排查问题

    public static <T> Result<T> success(T data) {
        Result<T> result = new Result<>();
        result.setCode(200);
        result.setMessage("Success");
        result.setData(data);
        result.setTraceId(MDC.get("traceId")); // 自动填充当前的 TraceId
        return result;
    }

    public static <T> Result<T> success(String msg, T data) {
        Result<T> result = new Result<>();
        result.setCode(200);
        result.setMessage(msg);
        result.setData(data);
        result.setTraceId(MDC.get("traceId"));
        return result;
    }

    public static <T> Result<T> error(Integer code, String message) {
        Result<T> result = new Result<>();
        result.setCode(code);
        result.setMessage(message);
        result.setTraceId(MDC.get("traceId"));
        return result;
    }

    public static <T> Result<T> error(String message) {
        return error(500, message);
    }
}