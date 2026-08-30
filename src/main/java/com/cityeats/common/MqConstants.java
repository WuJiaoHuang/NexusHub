package com.cityeats.common;

public final class MqConstants {

    public static final String VOUCHER_ORDER_EXCHANGE = "cityeats.voucher.order.exchange";
    public static final String VOUCHER_ORDER_QUEUE = "cityeats.voucher.order.queue";
    public static final String VOUCHER_ORDER_ROUTING_KEY = "voucher.order.create";

    private MqConstants() {
    }
}
