package com.cinemaweb.API.Cinema.Web.configuration;

import org.springframework.amqp.core.Binding;
import org.springframework.amqp.core.BindingBuilder;
import org.springframework.amqp.core.DirectExchange;
import org.springframework.amqp.core.Queue;
import org.springframework.amqp.core.QueueBuilder;
import org.springframework.amqp.core.TopicExchange;
import org.springframework.amqp.rabbit.connection.ConnectionFactory;
import org.springframework.amqp.rabbit.core.RabbitTemplate;
import org.springframework.amqp.support.converter.Jackson2JsonMessageConverter;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

/**
 * Topology RabbitMQ cho luồng phát vé sau thanh toán.
 *
 * booking.exchange (topic) --[booking.paid]--> booking.paid.q --(lỗi/cạn retry)--> booking.paid.dlx --> booking.paid.dlq
 *
 * Queue khai báo durable + gắn dead-letter exchange để message xử lý thất bại không mất mà rơi
 * vào DLQ cho việc kiểm tra/replay sau. JSON converter để (de)serialize BookingPaidEvent.
 */
@Configuration
public class RabbitConfig {

    public static final String EXCHANGE = "booking.exchange";
    public static final String QUEUE = "booking.paid.q";
    public static final String ROUTING_KEY = "booking.paid";

    public static final String DLX = "booking.paid.dlx";
    public static final String DLQ = "booking.paid.dlq";
    public static final String DLQ_ROUTING_KEY = "booking.paid.dlq";

    @Bean
    public TopicExchange bookingExchange() {
        return new TopicExchange(EXCHANGE);
    }

    @Bean
    public DirectExchange deadLetterExchange() {
        return new DirectExchange(DLX);
    }

    @Bean
    public Queue bookingPaidQueue() {
        return QueueBuilder.durable(QUEUE)
                .withArgument("x-dead-letter-exchange", DLX)
                .withArgument("x-dead-letter-routing-key", DLQ_ROUTING_KEY)
                .build();
    }

    @Bean
    public Queue deadLetterQueue() {
        return QueueBuilder.durable(DLQ).build();
    }

    @Bean
    public Binding bookingPaidBinding() {
        return BindingBuilder.bind(bookingPaidQueue()).to(bookingExchange()).with(ROUTING_KEY);
    }

    @Bean
    public Binding deadLetterBinding() {
        return BindingBuilder.bind(deadLetterQueue()).to(deadLetterExchange()).with(DLQ_ROUTING_KEY);
    }

    @Bean
    public Jackson2JsonMessageConverter jacksonMessageConverter() {
        return new Jackson2JsonMessageConverter();
    }

    // RabbitTemplate dùng JSON converter để publish event dưới dạng JSON.
    @Bean
    public RabbitTemplate rabbitTemplate(ConnectionFactory connectionFactory,
                                         Jackson2JsonMessageConverter converter) {
        RabbitTemplate template = new RabbitTemplate(connectionFactory);
        template.setMessageConverter(converter);
        return template;
    }
}
