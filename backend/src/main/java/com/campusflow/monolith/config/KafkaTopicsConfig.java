package com.campusflow.monolith.config;

import org.apache.kafka.clients.admin.NewTopic;
import org.apache.kafka.clients.consumer.ConsumerConfig;
import org.apache.kafka.common.serialization.StringDeserializer;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.kafka.config.ConcurrentKafkaListenerContainerFactory;
import org.springframework.kafka.config.TopicBuilder;
import org.springframework.kafka.core.ConsumerFactory;
import org.springframework.kafka.core.DefaultKafkaConsumerFactory;
import org.springframework.kafka.support.converter.StringJsonMessageConverter;

import java.util.HashMap;
import java.util.Map;

/**
 * Backend'in kullandığı Kafka topic'lerini uygulama başlangıcında otomatik oluşturur.
 * Eğer topic zaten varsa atlanır.
 *
 * Ayrıca consumer container factory'sini yapılandırır:
 * StringDeserializer + StringJsonMessageConverter kullanarak her @KafkaListener
 * metodunun @Payload parametre tipine göre otomatik JSON dönüşümü sağlar.
 */
@Configuration
public class KafkaTopicsConfig {

    @Bean
    public NewTopic enrollmentRequestedTopic(
            @Value("${app.kafka.topics.enrollment-requested}") String name) {
        return TopicBuilder.name(name).partitions(1).replicas(1).build();
    }

    @Bean
    public NewTopic enrollmentConfirmedTopic(
            @Value("${app.kafka.topics.enrollment-confirmed}") String name) {
        return TopicBuilder.name(name).partitions(1).replicas(1).build();
    }

    @Bean
    public NewTopic enrollmentRejectedTopic(
            @Value("${app.kafka.topics.enrollment-rejected}") String name) {
        return TopicBuilder.name(name).partitions(1).replicas(1).build();
    }

    /**
     * Consumer factory: StringDeserializer ile JSON string olarak okur.
     * StringJsonMessageConverter bu string'i @Payload parametre tipine dönüştürür.
     * Bu sayede farklı topic'lerdeki farklı event tipleri otomatik çözümlenir.
     */
    @Bean
    public ConcurrentKafkaListenerContainerFactory<String, String> kafkaListenerContainerFactory(
            @Value("${spring.kafka.bootstrap-servers}") String bootstrapServers,
            @Value("${spring.kafka.consumer.group-id}") String groupId,
            @Value("${spring.kafka.consumer.auto-offset-reset}") String autoOffsetReset) {

        Map<String, Object> props = new HashMap<>();
        props.put(ConsumerConfig.BOOTSTRAP_SERVERS_CONFIG, bootstrapServers);
        props.put(ConsumerConfig.GROUP_ID_CONFIG, groupId);
        props.put(ConsumerConfig.KEY_DESERIALIZER_CLASS_CONFIG, StringDeserializer.class);
        props.put(ConsumerConfig.VALUE_DESERIALIZER_CLASS_CONFIG, StringDeserializer.class);
        props.put(ConsumerConfig.AUTO_OFFSET_RESET_CONFIG, autoOffsetReset);

        ConsumerFactory<String, String> consumerFactory = new DefaultKafkaConsumerFactory<>(props);

        ConcurrentKafkaListenerContainerFactory<String, String> factory =
                new ConcurrentKafkaListenerContainerFactory<>();
        factory.setConsumerFactory(consumerFactory);
        factory.setRecordMessageConverter(new StringJsonMessageConverter());
        return factory;
    }
}
