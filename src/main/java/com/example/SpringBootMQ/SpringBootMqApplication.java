package com.example.SpringBootMQ;

import java.io.File;
import java.nio.file.Files;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.CommandLineRunner;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.jms.annotation.EnableJms;

import com.example.SpringBootMQ.service.JMSPersistenceService;

@SpringBootApplication
@EnableJms
public class SpringBootMqApplication implements CommandLineRunner{

	@Autowired
	private JMSPersistenceService jmsPersistenceService;
	
	public static void main(String[] args) {
		SpringApplication.run(SpringBootMqApplication.class, args);
	}

	@Override
	public void run(String... args) throws Exception {
		
		//Unmarshall the xml
		ClassLoader classLoader = getClass().getClassLoader();
		File inputFile = new File(classLoader.getResource("Input.xml").getFile());
		String message = new String(Files.readAllBytes(inputFile.toPath()));
		//messageSender.sendMessage(message);
		String response = jmsPersistenceService.submitStatement(message);
		System.out.println("Response received from Request-Reply :: ");
		System.out.println(response);
	}

}
