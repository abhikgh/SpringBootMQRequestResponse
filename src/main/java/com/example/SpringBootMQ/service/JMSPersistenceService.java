package com.example.SpringBootMQ.service;

import java.io.UnsupportedEncodingException;
import java.security.SecureRandom;
import java.util.Optional;
import java.util.stream.Stream;

import javax.jms.BytesMessage;
import javax.jms.Connection;
import javax.jms.Destination;
import javax.jms.JMSException;
import javax.jms.Message;
import javax.jms.MessageConsumer;
import javax.jms.MessageProducer;
import javax.jms.Queue;
import javax.jms.Session;
import javax.jms.TextMessage;

import org.springframework.stereotype.Service;

import com.example.SpringBootMQ.configuration.JMSConfig;
import com.ibm.mq.constants.MQConstants;
import com.ibm.msg.client.jms.JmsDestination;
import com.ibm.msg.client.wmq.WMQConstants;

import io.vavr.Function1;
import lombok.AllArgsConstructor;
import lombok.SneakyThrows;
import lombok.extern.slf4j.Slf4j;

@Service
@AllArgsConstructor
@Slf4j
public class JMSPersistenceService {

	private final JMSConfig mqConfig;

	/**
	 * Generates IBM MQ compliant message ID.
	 *
	 * @return byte array
	 */
	private static byte[] generateMsgId() {

		byte[] bytes = new byte[24];
		new SecureRandom().nextBytes(bytes);

		return bytes;
	}

	private static void sneakyClose(final AutoCloseable x) {
		try {
			x.close();
		} catch (Exception e) {
			log.error("Couldn't close Closeable: " + e);
		}
	}

	/**
	 * Producer HEX string representation of the byte[24] array IBM MQ message ID
	 *
	 * @param bytes IBM MQ message ID byte array
	 * @return HEX representation
	 */
	private static String byteArrayToHexString(final byte[] bytes) {
		return javax.xml.bind.DatatypeConverter.printHexBinary(bytes);
	}

	/**
	 * Sends the message to request queue
	 * 
	 * @param requestMessage
	 * @return
	 */
	@SneakyThrows(JMSException.class)
	public String sendStatementRequest(final String requestMessage) {
		String correlationId;

		Connection connection = null;
		Session session = null;
		MessageProducer producer = null;

		try {
			connection = mqConfig.ibmmq().getConfiguration().getConnectionFactory().createConnection();
			session = connection.createSession(false, Session.AUTO_ACKNOWLEDGE);
			Queue requestQueue = session.createQueue("queue:///" + mqConfig.getRequestQueue() + "?targetClient=1");

			// MQ msgId
			byte[] msgId = generateMsgId();

			// Enable write of MQMD fields. See documentation for further details.
			((JmsDestination) requestQueue).setBooleanProperty(WMQConstants.WMQ_MQMD_WRITE_ENABLED, true);
			producer = session.createProducer(requestQueue);

			// create TextMessage
			TextMessage message = session.createTextMessage(requestMessage);
			// set JMSDestination, MQSTR format and message id on the message
			message.setJMSDestination(requestQueue);
			message.setStringProperty(WMQConstants.JMS_IBM_FORMAT, MQConstants.MQFMT_STRING);
			message.setObjectProperty(WMQConstants.JMS_IBM_MQMD_MSGID, msgId);
			// replyToQueue
			Destination replyToDestination = session.createQueue(mqConfig.getReplyQueue());
			message.setJMSReplyTo(replyToDestination);

			// hex representation of the message id byte array
			correlationId = byteArrayToHexString(msgId);
			log.info("sendStatementRequest, msgId={}, replyToQueue={}", correlationId, mqConfig.getReplyQueue());

			// Start the connection and send the message
			connection.start();
			producer.send(message);

			return correlationId;
		} finally {
			Stream.of(producer, session, connection).map(Optional::ofNullable)
					.forEach(x -> x.ifPresent(JMSPersistenceService::sneakyClose));
		}
	}

	/**
	 * Receives the message from reply queue
	 * 
	 * @param messageId
	 * @return
	 */
	@SneakyThrows({ JMSException.class, Exception.class })
	public String receiveStatementResponse(final String messageId) {
		log.info("Receiving the Response from Reply Queue for Message Id {}", messageId);

		String responseMessage;

		Connection connection = null;
		Session session = null;
		MessageConsumer consumer = null;

		try {
			connection = mqConfig.ibmmq().getConfiguration().getConnectionFactory().createConnection();
			session = connection.createSession(false, Session.AUTO_ACKNOWLEDGE);

			Queue responseQueue = session.createQueue("queue:///" + mqConfig.getReplyQueue());

			// Enable read of MQMD fields. See documentation for further details.
			((JmsDestination) responseQueue).setBooleanProperty(WMQConstants.WMQ_MQMD_READ_ENABLED, true);

			// message selector
			consumer = session.createConsumer(responseQueue, "JMSCorrelationID = 'ID:" + messageId + "'");

			// Start the connection and receive a message from the queue
			connection.start();
			Message message = consumer.receive(mqConfig.getReceiveTimeout());

			/* process response */
			if (message == null) {
				log.error("method=receiveStatementResponse, no response from Listener service due to Timeout");
				return null;
			}

			responseMessage = convertJmsMessageToString(message);

			return responseMessage;
		} finally {
			Stream.of(consumer, session, connection).map(Optional::ofNullable)
					.forEach(x -> x.ifPresent(JMSPersistenceService::sneakyClose));
		}
	}

	/**
	 * Detect message type and convert to text
	 * 
	 * @param message
	 * @return
	 */
	@SneakyThrows({ UnsupportedEncodingException.class, Exception.class })
	static String convertJmsMessageToString(final Message message) {

		if (message instanceof TextMessage) {
			return ((TextMessage) message).getText();
		} else if (message instanceof BytesMessage) {
			BytesMessage bytesMessage = (BytesMessage) message;
			final int messageLength = (int) bytesMessage.getBodyLength();

			byte[] textBytes = new byte[messageLength];
			bytesMessage.readBytes(textBytes, messageLength);
			String messageCharSet = bytesMessage.getStringProperty(WMQConstants.JMS_IBM_CHARACTER_SET);

			return new String(textBytes, messageCharSet);

		} else {
			throw new Exception("Unexpected message type");
		}
	}

	/**
	 * Orchestrates request-reply
	 * 
	 * @param message
	 * @return
	 */
	public String submitStatement(final String message) {

		log.info("Sending the XML String to Request Queue");
		Function1<String, String> sendAndReceive = ((Function1<String, String>) this::sendStatementRequest)
				.andThen(this::receiveStatementResponse);

		return Function1.lift(sendAndReceive).apply(message).getOrElse("no response");

	}

}
