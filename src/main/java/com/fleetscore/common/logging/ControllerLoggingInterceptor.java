package com.fleetscore.common.logging;

import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Component;
import org.springframework.web.method.HandlerMethod;
import org.springframework.web.servlet.HandlerInterceptor;

@Component
public class ControllerLoggingInterceptor implements HandlerInterceptor {

    private static final String START_TIME_ATTRIBUTE = "controllerStartTime";
    private static final Logger log = LoggerFactory.getLogger(ControllerLoggingInterceptor.class);

    @Override
    public boolean preHandle(HttpServletRequest request, HttpServletResponse response, Object handler) {
        request.setAttribute(START_TIME_ATTRIBUTE, System.currentTimeMillis());
        String target = handler instanceof HandlerMethod handlerMethod
                ? handlerMethod.getBeanType().getSimpleName() + "#" + handlerMethod.getMethod().getName()
                : handler.getClass().getSimpleName();
        log.info("Controller request: {} {} -> {}", request.getMethod(), request.getRequestURI(), target);
        return true;
    }

    @Override
    public void afterCompletion(HttpServletRequest request,
                                HttpServletResponse response,
                                Object handler,
                                Exception ex) {
        Object startTime = request.getAttribute(START_TIME_ATTRIBUTE);
        long durationMs = startTime instanceof Long start ? System.currentTimeMillis() - start : -1;
        log.info("Controller response: {} {} status={} durationMs={}",
                request.getMethod(),
                request.getRequestURI(),
                response.getStatus(),
                durationMs);
    }
}
