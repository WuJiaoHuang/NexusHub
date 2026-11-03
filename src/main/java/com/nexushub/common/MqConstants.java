package com.nexushub.common;

public final class MqConstants {

    // Wire-level names intentionally remain stable across the brand/package migration.
    public static final String EVENT_REGISTRATION_EXCHANGE = "campuslink.event.registration.exchange";
    public static final String EVENT_REGISTRATION_QUEUE = "campuslink.event.registration.queue";
    public static final String EVENT_REGISTRATION_ROUTING_KEY = "event.registration.create";
    public static final String EVENT_REGISTRATION_DLX = "campuslink.event.registration.dlx";
    public static final String EVENT_REGISTRATION_DLQ = "campuslink.event.registration.dlq";
    public static final String EVENT_REGISTRATION_DLQ_ROUTING_KEY = "event.registration.failed";

    private MqConstants() {
    }
}
