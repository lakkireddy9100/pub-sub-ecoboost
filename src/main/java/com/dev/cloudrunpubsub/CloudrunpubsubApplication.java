package com.dev.cloudrunpubsub;


import lombok.extern.slf4j.Slf4j;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.cloud.stream.annotation.EnableBinding;
import org.springframework.cloud.stream.annotation.StreamListener;

@SpringBootApplication
@EnableBinding(MessageConsumerSource.class)
@Slf4j
public class CloudrunpubsubApplication {

	public static void main(String[] args) {
		SpringApplication.run(CloudrunpubsubApplication.class, args);
	}


	@StreamListener(target = MessageConsumerSource.MESSAGE_CHANNEL)
	public void fetchMessageFromQueue(String message){

		log.info("Name from MQ ::" + message);
	}

}
