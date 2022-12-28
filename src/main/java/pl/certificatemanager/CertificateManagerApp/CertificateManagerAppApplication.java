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
//
//		System.out.println("*************************************************");
//
////		XML FILE PARSER USING DOM
//		try {
//			File xmlDoc = new File("C:/Users/danie/Downloads/customers.xml");
//			DocumentBuilderFactory factory = DocumentBuilderFactory.newInstance();
//			DocumentBuilder builder = factory.newDocumentBuilder();
//			Document doc = builder.parse(xmlDoc);
//
////			Read root element
//			System.out.println("Root element: " + doc.getDocumentElement().getNodeName());
//
////			Read array of customers elements
////			this array is called NodeList
//			NodeList customerList = doc.getElementsByTagName("customer");
//
//			for (int i = 0; i < customerList.getLength(); i++) {
//				Node customer = customerList.item(i);
//				System.out.println("Node name: " + customer.getNodeName());
//				if (customer.getNodeType() == Node.ELEMENT_NODE) {
//					Element elementCustomer = (Element) customer;
//					System.out.println("	First name: " + elementCustomer.getElementsByTagName("firstName").item(0).getTextContent());
//					System.out.println("	Last name: " + elementCustomer.getElementsByTagName("lastName").item(0).getTextContent());
//					System.out.println("	Phone number: " + elementCustomer.getElementsByTagName("phoneNumber").item(0).getTextContent());
//					System.out.println("	Email: " + elementCustomer.getElementsByTagName("email").item(0).getTextContent());
//					System.out.println("	City: " + elementCustomer.getElementsByTagName("city").item(0).getTextContent());
//					System.out.println(" ");
//
//					NodeList invoiceList = elementCustomer.getElementsByTagName("invoice");
//					for (int j = 0; j < invoiceList.getLength(); j++) {
//						Node invoice = invoiceList.item(j);
//						System.out.println("Node name: " + invoice.getNodeName());
//						if (invoice.getNodeType() == Node.ELEMENT_NODE) {
//							Element elementInvoice = (Element) invoice;
//							System.out.println("		Invoice number: " + elementInvoice.getElementsByTagName("invoiceNumber").item(0).getTextContent());
//							System.out.println("		Date of agreement: " + elementInvoice.getElementsByTagName("dateOfAgreement").item(0).getTextContent());
//							System.out.println("		Status: " + elementInvoice.getElementsByTagName("status").item(0).getTextContent());
//							System.out.println(" ");
//
//							NodeList certificatesList= elementInvoice.getElementsByTagName("certificate");
//							for (int y = 0; y < certificatesList.getLength(); y++) {
//								Node certificate = certificatesList.item(y);
//								System.out.println("Node name: " + certificate.getNodeName());
//								if (certificate.getNodeType() == Node.ELEMENT_NODE) {
//									Element elementCertificate = (Element) certificate;
//									System.out.println("			Serial number: " + elementCertificate.getElementsByTagName("serialNumber").item(0).getTextContent());
//									System.out.println("			Valid from: " + elementCertificate.getElementsByTagName("validFrom").item(0).getTextContent());
//									System.out.println("			Valid to: " + elementCertificate.getElementsByTagName("validTo").item(0).getTextContent());
//									System.out.println("			Card number: " + elementCertificate.getElementsByTagName("cardNumber").item(0).getTextContent());
//									System.out.println("			Card type: " + elementCertificate.getElementsByTagName("cardType").item(0).getTextContent());
//									System.out.println(" ");
//								}
//							}
//						}
//					}
//					System.out.println("*************************************************");
//				}
//			}
//		} catch (Exception e) {
//
//		}
	}
}
