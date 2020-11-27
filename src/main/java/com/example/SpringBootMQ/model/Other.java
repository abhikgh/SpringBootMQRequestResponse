package com.example.SpringBootMQ.model;

import javax.xml.bind.annotation.XmlAccessType;
import javax.xml.bind.annotation.XmlAccessorType;
import javax.xml.bind.annotation.XmlElement;

import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
@XmlAccessorType(XmlAccessType.FIELD)
public class Other {

	@XmlElement(name = "ServiceIP")
	private String serviceIP;

	@XmlElement(name = "ServiceTime")
	private String serviceTime;
}
