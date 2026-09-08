package com.campuslink.utils;

public final class RedisConstants {

    public static final String LOGIN_CODE_KEY = "login:code:";
    public static final Long LOGIN_CODE_TTL = 2L;
    public static final String LOGIN_USER_KEY = "login:token:";
    public static final Long LOGIN_USER_TTL = 30L;

    public static final Long CACHE_NULL_TTL = 2L;
    public static final Long CACHE_PLACE_TTL = 30L;
    public static final String CACHE_PLACE_KEY = "cache:place:";
    public static final String CACHE_PLACE_CATEGORY_KEY = "cache:place:type";
    public static final String LOCK_PLACE_KEY = "lock:place:";
    public static final Long LOCK_SHOP_TTL = 10L;

    public static final String CAMPUS_PLACE_GEO_KEY = "geo:place:type:";
    public static final String POST_LIKED_KEY = "post:liked:";
    public static final String FEED_KEY = "feed:";
    public static final String SIGN_KEY = "sign:";
    public static final String EVENT_REMAINING_SLOTS_KEY = "event:slots:";
    public static final String EVENT_REGISTRATION_KEY = "event:registration:";
    public static final String EVENT_REGISTRATION_COMPENSATED_KEY = "event:registration:compensated:";

    private RedisConstants() {
    }
}
