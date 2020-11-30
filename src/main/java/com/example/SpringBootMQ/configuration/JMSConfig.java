package com.example.SpringBootMQ.configuration;

import javax.jms.JMSException;

import org.apache.camel.component.jms.JmsComponent;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

import com.ibm.mq.jms.MQQueueConnectionFactory;
import com.ibm.msg.client.wmq.WMQConstants;

import lombok.Getter;
import lombok.Setter;

@Configuration
@Getter
@Setter
public class JMSConfig {

	@Value("${ibm.mq.queueManager}")
	private String queueManager;

	@Value("${ibm.mq.channel}")
	private String channel;

	@Value("${ibm.mq.connName}")
	private String connName;

	@Value("${ibm.mq.userAuthentication}")
	private boolean userAuthentication;

	@Value("${ibm.mq.user}")
	private String username;

	@Value("${ibm.mq.password}")
	private String password;

	@Value("${ibm.mq.receiveTimeout}")
	private long receiveTimeout;

	@Value("${ibm.mq.transportType}")
	private int transportType;
	
	@Value("${ibm.mq.appRequestQueue}")
	private String requestQueue;

	@Value("${ibm.mq.appResponseQueue}")
	private String replyQueue;

	@Value("${spring.application.name}")
	private String applicationName;

	@Bean
	public JmsComponent ibmmq() {
		System.out.println("MQ Connection Factory Establishment Started...");
		MQQueueConnectionFactory factory = new MQQueueConnectionFactory();
		try {
			factory.setQueueManager(queueManager);
			factory.setConnectionNameList(connName);
			factory.setChannel(channel);
			factory.setBooleanProperty(WMQConstants.USER_AUTHENTICATION_MQCSP, userAuthentication);
			factory.setStringProperty(WMQConstants.USERID, username);
			factory.setStringProperty(WMQConstants.PASSWORD, password);
			factory.setTransportType(transportType);
		} catch (JMSException jmsException) {
			System.out.println("Failed while establishing connection to MQ Connection Factory!!!!");
			jmsException.printStackTrace();
		}
		JmsComponent ibmmq = new JmsComponent();
		ibmmq.setConnectionFactory(factory);
		System.out.println("MQ Connection Factory Establishment Ended.....");
		return ibmmq;
	}

}
