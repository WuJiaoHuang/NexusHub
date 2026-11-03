package com.nexushub.utils;

import com.nexushub.dto.UserDTO;

public final class UserHolder {

    private static final ThreadLocal<UserDTO> TL = new ThreadLocal<>();

    private UserHolder() {
    }

    public static void saveUser(UserDTO user) {
        TL.set(user);
    }

    public static UserDTO getUser() {
        return TL.get();
    }

    public static void removeUser() {
        TL.remove();
    }
}
