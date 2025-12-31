package com.group.common.interceptor;

import cn.hutool.core.util.IdUtil;
import cn.hutool.core.util.StrUtil;
import org.slf4j.MDC;
import org.springframework.web.servlet.HandlerInterceptor;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

public class TraceIdInterceptor implements HandlerInterceptor {

    private static final String TRACE_ID_HEADER = "X-Trace-Id";
    private static final String MDC_KEY = "traceId";

    @Override
    public boolean preHandle(HttpServletRequest request, HttpServletResponse response, Object handler) {
        // 1. 尝试从请求头获取 TraceId (服务间调用会透传)
        String traceId = request.getHeader(TRACE_ID_HEADER);

        // 2. 如果没有（外部请求），则生成新的 UUID
        if (StrUtil.isBlank(traceId)) {
            traceId = IdUtil.simpleUUID();
        }

        // 3. 放入 MDC，这样 log.info() 就能自动打印出 traceId
        MDC.put(MDC_KEY, traceId);

        // 4. 同时放入 Response Header，方便客户端查看
        response.setHeader(TRACE_ID_HEADER, traceId);

        return true;
    }

    @Override
    public void afterCompletion(HttpServletRequest request, HttpServletResponse response, Object handler, Exception ex) {
        // 5. 请求结束，清理 ThreadLocal，防止内存泄漏或数据污染
        MDC.remove(MDC_KEY);
    }
}