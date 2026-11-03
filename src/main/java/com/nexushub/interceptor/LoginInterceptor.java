package com.nexushub.interceptor;

import com.nexushub.dto.UserDTO;
import com.nexushub.utils.UserHolder;
import com.nexushub.security.PublicEndpoint;
import org.springframework.web.method.HandlerMethod;
import org.springframework.web.servlet.HandlerInterceptor;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

public class LoginInterceptor implements HandlerInterceptor {

    @Override
    public boolean preHandle(HttpServletRequest request, HttpServletResponse response, Object handler) throws Exception {
        if (handler instanceof HandlerMethod
                && ((HandlerMethod) handler).hasMethodAnnotation(PublicEndpoint.class)) {
            return true;
        }
        UserDTO user = UserHolder.getUser();
        if (user == null) {
            response.setStatus(HttpServletResponse.SC_UNAUTHORIZED);
            return false;
        }
        return true;
    }
}
