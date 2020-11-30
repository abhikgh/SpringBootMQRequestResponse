package com.example.SpringBootMQ;

import java.io.File;
import java.nio.file.Files;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.CommandLineRunner;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.cloud.context.config.annotation.RefreshScope;
import org.springframework.jms.annotation.EnableJms;

import com.example.SpringBootMQ.service.JMSPersistenceService;

@SpringBootApplication
@EnableJms
@RefreshScope
public class SpringBootMqApplication implements CommandLineRunner {

	@Autowired
	private JMSPersistenceService jmsPersistenceService;
	
	@Value("${test.data}")
	private String testData;

	public static void main(String[] args) {
		SpringApplication.run(SpringBootMqApplication.class, args);
	}

	@Override
	public void run(String... args) throws Exception {
		
		System.out.println("test data ::" + testData);

		// Unmarshall the xml
		ClassLoader classLoader = getClass().getClassLoader();
		File inputFile = new File(classLoader.getResource("Input.xml").getFile());
		String message = new String(Files.readAllBytes(inputFile.toPath()));
		String response = jmsPersistenceService.submitStatement(message);
		System.out.println("Response received from Request-Reply :: ");
		System.out.println(response);
	}

}
