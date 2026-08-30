package com.cityeats.config;

import com.cityeats.common.MqConstants;
import org.springframework.amqp.core.Binding;
import org.springframework.amqp.core.BindingBuilder;
import org.springframework.amqp.core.DirectExchange;
import org.springframework.amqp.core.Queue;
import org.springframework.amqp.rabbit.connection.ConnectionFactory;
import org.springframework.amqp.rabbit.core.RabbitTemplate;
import org.springframework.amqp.support.converter.Jackson2JsonMessageConverter;
import org.springframework.amqp.support.converter.MessageConverter;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@Configuration
public class QueueConfig {

    @Bean
    public DirectExchange voucherOrderExchange() {
        return new DirectExchange(MqConstants.VOUCHER_ORDER_EXCHANGE, true, false);
    }

    @Bean
    public Queue voucherOrderQueue() {
        return new Queue(MqConstants.VOUCHER_ORDER_QUEUE, true);
    }

    @Bean
    public Binding voucherOrderBinding(Queue voucherOrderQueue, DirectExchange voucherOrderExchange) {
        return BindingBuilder.bind(voucherOrderQueue)
                .to(voucherOrderExchange)
                .with(MqConstants.VOUCHER_ORDER_ROUTING_KEY);
    }

    @Bean
    public MessageConverter messageConverter() {
        return new Jackson2JsonMessageConverter();
    }

    @Bean
    public RabbitTemplate rabbitTemplate(ConnectionFactory connectionFactory, MessageConverter messageConverter) {
        RabbitTemplate rabbitTemplate = new RabbitTemplate(connectionFactory);
        rabbitTemplate.setMessageConverter(messageConverter);
        return rabbitTemplate;
    }
}

