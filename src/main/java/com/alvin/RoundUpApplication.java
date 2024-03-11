package com.alvin;

import jakarta.jms.ConnectionFactory;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.boot.autoconfigure.jms.DefaultJmsListenerContainerFactoryConfigurer;
import org.springframework.context.annotation.Bean;
import org.springframework.jms.annotation.EnableJms;
import org.springframework.jms.config.DefaultJmsListenerContainerFactory;
import org.springframework.jms.config.JmsListenerContainerFactory;
import org.springframework.jms.support.converter.MappingJackson2MessageConverter;
import org.springframework.jms.support.converter.MessageConverter;
import org.springframework.jms.support.converter.MessageType;
import org.springframework.transaction.PlatformTransactionManager;

import java.net.http.HttpClient;
import java.time.Duration;

@EnableJms
@SpringBootApplication
public class RoundUpApplication {

	public static void main(String[] args) {
		SpringApplication.run(RoundUpApplication.class, args);
	}

	@Bean
	public HttpClient httpClient() {
		return HttpClient.newBuilder().connectTimeout(Duration.ofSeconds(30)).build();
	}

	@Bean
	public JmsListenerContainerFactory<?> myFactory(ConnectionFactory connectionFactory,
													DefaultJmsListenerContainerFactoryConfigurer configurer) {

		DefaultJmsListenerContainerFactory factory = new DefaultJmsListenerContainerFactory();
		configurer.configure(factory, connectionFactory);
		return factory;
	}

	@Bean
	public MessageConverter jacksonJmsMessageConverter() {
		MappingJackson2MessageConverter converter = new MappingJackson2MessageConverter();
		converter.setTargetType(MessageType.TEXT);
		converter.setTypeIdPropertyName("_type");
		return converter;
	}
}
