package com.campuslink.config;

import com.campuslink.common.MqConstants;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.SerializationFeature;
import com.fasterxml.jackson.datatype.jsr310.JavaTimeModule;
import org.springframework.amqp.core.Binding;
import org.springframework.amqp.core.BindingBuilder;
import org.springframework.amqp.core.DirectExchange;
import org.springframework.amqp.core.Queue;
import org.springframework.amqp.core.QueueBuilder;
import org.springframework.amqp.rabbit.connection.ConnectionFactory;
import org.springframework.amqp.rabbit.core.RabbitTemplate;
import org.springframework.amqp.support.converter.Jackson2JsonMessageConverter;
import org.springframework.amqp.support.converter.MessageConverter;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@Configuration
public class QueueConfig {

    @Bean
    public DirectExchange eventRegistrationExchange() {
        return new DirectExchange(MqConstants.EVENT_REGISTRATION_EXCHANGE, true, false);
    }

    @Bean
    public Queue eventRegistrationQueue() {
        return QueueBuilder.durable(MqConstants.EVENT_REGISTRATION_QUEUE)
                .deadLetterExchange(MqConstants.EVENT_REGISTRATION_DLX)
                .deadLetterRoutingKey(MqConstants.EVENT_REGISTRATION_DLQ_ROUTING_KEY)
                .build();
    }

    @Bean
    public Binding eventRegistrationBinding(Queue eventRegistrationQueue, DirectExchange eventRegistrationExchange) {
        return BindingBuilder.bind(eventRegistrationQueue)
                .to(eventRegistrationExchange)
                .with(MqConstants.EVENT_REGISTRATION_ROUTING_KEY);
    }

    @Bean
    public DirectExchange eventRegistrationDeadLetterExchange() {
        return new DirectExchange(MqConstants.EVENT_REGISTRATION_DLX, true, false);
    }

    @Bean
    public Queue eventRegistrationDeadLetterQueue() {
        return QueueBuilder.durable(MqConstants.EVENT_REGISTRATION_DLQ).build();
    }

    @Bean
    public Binding eventRegistrationDeadLetterBinding(Queue eventRegistrationDeadLetterQueue, DirectExchange eventRegistrationDeadLetterExchange) {
        return BindingBuilder.bind(eventRegistrationDeadLetterQueue)
                .to(eventRegistrationDeadLetterExchange)
                .with(MqConstants.EVENT_REGISTRATION_DLQ_ROUTING_KEY);
    }

    @Bean
    public MessageConverter messageConverter() {
        ObjectMapper objectMapper = new ObjectMapper();
        objectMapper.registerModule(new JavaTimeModule());
        objectMapper.disable(SerializationFeature.WRITE_DATES_AS_TIMESTAMPS);
        return new Jackson2JsonMessageConverter(objectMapper);
    }

    @Bean
    public RabbitTemplate rabbitTemplate(ConnectionFactory connectionFactory, MessageConverter messageConverter) {
        RabbitTemplate rabbitTemplate = new RabbitTemplate(connectionFactory);
        rabbitTemplate.setMessageConverter(messageConverter);
        return rabbitTemplate;
    }
}

