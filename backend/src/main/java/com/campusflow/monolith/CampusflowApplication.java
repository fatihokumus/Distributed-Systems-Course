package com.campusflow.monolith;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.context.annotation.Bean;
import org.springframework.retry.annotation.EnableRetry;
import org.springframework.web.client.RestTemplate; 
@EnableRetry
@SpringBootApplication
public class CampusflowApplication {

    public static void main(String[] args) {
        SpringApplication.run(CampusflowApplication.class, args);
    }

    // Bu metod, Monolith'in diğer servislere istek atabilmesini sağlar
@Bean
public RestTemplate restTemplate(org.springframework.boot.web.client.RestTemplateBuilder builder) {
    return builder
            .setConnectTimeout(java.time.Duration.ofSeconds(1)) // Bağlanma süresi 1 sn
            .setReadTimeout(java.time.Duration.ofSeconds(2))    // Cevap bekleme süresi 20 sn (Sınırımız bu!)
            .build();
}
}