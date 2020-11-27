package com.example.SpringBootMQ.model;


import javax.xml.bind.annotation.XmlAccessType;
import javax.xml.bind.annotation.XmlAccessorType;
import javax.xml.bind.annotation.XmlElement;
import javax.xml.bind.annotation.XmlRootElement;

import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
@XmlAccessorType(XmlAccessType.FIELD)
@XmlRootElement
public class AppData {

	@XmlElement(name = "Conversion")
	private Conversion conversion;
	
	@XmlElement(name = "Other")
	private Other other;
}
