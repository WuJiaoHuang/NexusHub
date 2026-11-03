package com.nexushub.interceptor;

import com.nexushub.controller.PlaceController;
import com.nexushub.dto.UserDTO;
import com.nexushub.entity.Place;
import com.nexushub.utils.UserHolder;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.Test;
import org.springframework.mock.web.MockHttpServletRequest;
import org.springframework.mock.web.MockHttpServletResponse;
import org.springframework.web.method.HandlerMethod;

import static org.assertj.core.api.Assertions.assertThat;

class AdminAuthorizationInterceptorTest {
    @AfterEach
    void clearUser() {
        UserHolder.removeUser();
    }

    @Test
    void emptyAllowlistDeniesEvenAuthenticatedUser() throws Exception {
        UserDTO user = new UserDTO();
        user.setId(1L);
        UserHolder.saveUser(user);
        MockHttpServletResponse response = new MockHttpServletResponse();
        HandlerMethod handler = new HandlerMethod(new PlaceController(), "savePlace", Place.class);

        assertThat(new AdminAuthorizationInterceptor("").preHandle(new MockHttpServletRequest(), response, handler))
                .isFalse();
        assertThat(response.getStatus()).isEqualTo(403);
    }

    @Test
    void configuredAdminWithoutAuthenticatedSessionIsRejected() throws Exception {
        MockHttpServletResponse response = new MockHttpServletResponse();
        HandlerMethod handler = new HandlerMethod(new PlaceController(), "savePlace", Place.class);

        assertThat(new AdminAuthorizationInterceptor("1").preHandle(new MockHttpServletRequest(), response, handler))
                .isFalse();
        assertThat(response.getStatus()).isEqualTo(401);
    }
}
