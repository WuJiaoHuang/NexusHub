package com.nexushub.interceptor;

import com.nexushub.dto.UserDTO;
import com.nexushub.security.AdminOnly;
import com.nexushub.utils.UserHolder;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.core.annotation.AnnotationUtils;
import org.springframework.stereotype.Component;
import org.springframework.web.method.HandlerMethod;
import org.springframework.web.servlet.HandlerInterceptor;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.util.Arrays;
import java.util.Set;
import java.util.stream.Collectors;

/** Server-managed allowlist; empty configuration denies all administrative writes. */
@Component
public class AdminAuthorizationInterceptor implements HandlerInterceptor {
    private final Set<Long> adminUserIds;

    public AdminAuthorizationInterceptor(@Value("${nexushub.security.admin-user-ids:}") String userIds) {
        adminUserIds = Arrays.stream(userIds.split(","))
                .map(String::trim).filter(value -> !value.isEmpty())
                .map(Long::valueOf).collect(Collectors.toSet());
    }

    @Override
    public boolean preHandle(HttpServletRequest request, HttpServletResponse response, Object handler) {
        if (!(handler instanceof HandlerMethod)) {
            return true;
        }
        HandlerMethod method = (HandlerMethod) handler;
        if (!method.hasMethodAnnotation(AdminOnly.class)
                && AnnotationUtils.findAnnotation(method.getBeanType(), AdminOnly.class) == null) {
            return true;
        }
        UserDTO user = UserHolder.getUser();
        if (user == null) {
            response.setStatus(HttpServletResponse.SC_UNAUTHORIZED);
            return false;
        }
        if (!adminUserIds.contains(user.getId())) {
            response.setStatus(HttpServletResponse.SC_FORBIDDEN);
            return false;
        }
        return true;
    }
}
