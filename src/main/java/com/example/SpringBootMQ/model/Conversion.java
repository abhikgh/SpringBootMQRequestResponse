package com.example.SpringBootMQ.model;

import javax.xml.bind.annotation.XmlAccessType;
import javax.xml.bind.annotation.XmlAccessorType;
import javax.xml.bind.annotation.XmlElement;

import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
@XmlAccessorType(XmlAccessType.FIELD)
public class Conversion {

	@XmlElement(name = "Celsius")
	private String celsius;

	@XmlElement(name = "Farenheit")
	private String farenheit;

	@XmlElement(name = "Miles")
	private String miles;

	@XmlElement(name = "Kilometer")
	private String kilometer;

	@XmlElement(name = "Kilogram")
	private String kilogram;

	@XmlElement(name = "Pound")
	private String pound;

}
