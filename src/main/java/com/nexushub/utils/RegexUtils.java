package com.nexushub.utils;

import cn.hutool.core.util.StrUtil;

public final class RegexUtils {

    private RegexUtils() {
    }

    public static boolean isPhoneInvalid(String phone) {
        return StrUtil.isBlank(phone) || !phone.matches(RegexPatterns.PHONE_REGEX);
    }
}
