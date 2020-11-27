package com.example.SpringBootMQ.messaging;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.jms.core.JmsTemplate;
import org.springframework.stereotype.Component;

import lombok.extern.slf4j.Slf4j;

@Component
@Slf4j
public class MessageSender {

	@Value("${ibm.mq.appRequestQueue}")
	private String appRequestQueue;
	
	@Autowired
	private JmsTemplate jmsTemplate;

	public void sendMessage(String message) {
		try {
			log.info("Before sending...");
			jmsTemplate.convertAndSend(appRequestQueue, message);
			log.info("Message sent successfully");
		} catch (Exception e) {
			e.printStackTrace();
		}
	}

}
