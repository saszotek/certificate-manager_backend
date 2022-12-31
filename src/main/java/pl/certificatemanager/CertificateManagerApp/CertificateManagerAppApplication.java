package pl.certificatemanager.CertificateManagerApp;

import org.simplejavamail.api.email.Email;
import org.simplejavamail.converter.EmailConverter;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.w3c.dom.Document;
import org.w3c.dom.Element;
import org.w3c.dom.Node;
import org.w3c.dom.NodeList;

import javax.xml.parsers.DocumentBuilder;
import javax.xml.parsers.DocumentBuilderFactory;
import java.io.*;

@SpringBootApplication
public class CertificateManagerAppApplication {
	public static void main(String[] args) {
		SpringApplication.run(CertificateManagerAppApplication.class, args);
		System.out.println("*************************************************");
		System.out.println("*************************************************");
		System.out.println("*************************************************");

////		READING EML FILE
//		File emlFile = new File("C:/Users/danie/Downloads/dupa.eml");
//		Email email = EmailConverter.emlToEmail(emlFile);
//		System.out.println(email.getPlainText());
	}
}
