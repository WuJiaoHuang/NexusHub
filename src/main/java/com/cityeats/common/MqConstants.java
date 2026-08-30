package com.cityeats.common;

public final class MqConstants {

    public static final String VOUCHER_ORDER_EXCHANGE = "cityeats.voucher.order.exchange";
    public static final String VOUCHER_ORDER_QUEUE = "cityeats.voucher.order.queue";
    public static final String VOUCHER_ORDER_ROUTING_KEY = "voucher.order.create";
    public static final String VOUCHER_ORDER_DLX = "cityeats.voucher.order.dlx";
    public static final String VOUCHER_ORDER_DLQ = "cityeats.voucher.order.dlq";
    public static final String VOUCHER_ORDER_DLQ_ROUTING_KEY = "voucher.order.failed";

    private MqConstants() {
    }
}
