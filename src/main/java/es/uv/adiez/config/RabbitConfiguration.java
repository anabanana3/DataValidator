package es.uv.adiez.config;

import org.springframework.amqp.core.Binding;
import org.springframework.amqp.core.BindingBuilder;
import org.springframework.amqp.core.Queue;
import org.springframework.amqp.core.TopicExchange;
import org.springframework.amqp.rabbit.connection.CachingConnectionFactory;
import org.springframework.amqp.rabbit.core.RabbitAdmin;
import org.springframework.amqp.rabbit.core.RabbitTemplate;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

import es.uv.adiez.domain.File;

@Configuration
public class RabbitConfiguration {

		@Value("${jsa.rabbitmq.exchange}")
		private String topicExchangeName;

		@Value("${jsa.rabbitmq.queue}")
		private String queueName;

		@Value("${jsa.rabbitmq.routingKey}")
		private String routingKey;
		@Value("${jsa.rabbitmq.host}")
		private String host;
		
		@Bean
	    public CachingConnectionFactory connectionFactory() {
	        return new CachingConnectionFactory(host);
	    }

		@Bean
	    public RabbitAdmin amqpAdmin() {
	        return new RabbitAdmin(connectionFactory());
	    }

	    @Bean
	    public RabbitTemplate rabbitTemplate() {
	        return new RabbitTemplate(connectionFactory());
	    }
		@Bean
		public TopicExchange exchange() {
			return new TopicExchange(topicExchangeName);
		}

		@Bean
		public Queue queue() {
			return new Queue(queueName, false);
		}

		@Bean
		public Binding binding(Queue queue, TopicExchange exchange) {
			return BindingBuilder.bind(queue).to(exchange).with(routingKey);
		}
		
		public void sendData(Object message) {
	        rabbitTemplate().convertAndSend(this.topicExchangeName, this.routingKey, message);

		}

}
