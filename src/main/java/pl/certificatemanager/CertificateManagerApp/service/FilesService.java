package pl.certificatemanager.CertificateManagerApp.service;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.simplejavamail.api.email.Email;
import org.simplejavamail.converter.EmailConverter;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;
import org.w3c.dom.Document;
import org.w3c.dom.Element;
import org.w3c.dom.Node;
import org.w3c.dom.NodeList;
import pl.certificatemanager.CertificateManagerApp.exception.CertificateAlreadySavedException;
import pl.certificatemanager.CertificateManagerApp.exception.CustomerAlreadySavedException;
import pl.certificatemanager.CertificateManagerApp.exception.InvoiceAlreadySavedException;
import pl.certificatemanager.CertificateManagerApp.model.Certificate;
import pl.certificatemanager.CertificateManagerApp.model.Customer;
import pl.certificatemanager.CertificateManagerApp.model.Invoice;
import pl.certificatemanager.CertificateManagerApp.repository.CertificateRepo;
import pl.certificatemanager.CertificateManagerApp.repository.CustomerRepo;
import pl.certificatemanager.CertificateManagerApp.repository.InvoiceRepo;

import javax.transaction.Transactional;
import javax.xml.parsers.DocumentBuilder;
import javax.xml.parsers.DocumentBuilderFactory;
import java.io.File;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.text.SimpleDateFormat;
import java.util.Date;

@Service
@RequiredArgsConstructor
@Transactional
@Slf4j
public class FilesService {
    private final CustomerRepo customerRepo;
    private final InvoiceRepo invoiceRepo;
    private final CertificateRepo certificateRepo;
    private final CustomerService customerService;
    private final InvoiceService invoiceService;

    private final String filePath = "C:\\Users\\danie\\IdeaProjects\\CertificateManagerApp\\src\\main\\java\\pl\\certificatemanager\\CertificateManagerApp\\files\\";

    public void saveFile(MultipartFile file) {
        try {
            Files.copy(file.getInputStream(), Path.of(filePath + file.getOriginalFilename()));
        } catch (Exception e) {
            throw new RuntimeException("Could not store the file. Error: " + e.getMessage());
        }
    }

    public void saveDataFromImport() {
//        READING EML FILE
//        File emlFile = new File("C:/Users/danie/Downloads/dupa.eml");
//        Email email = EmailConverter.emlToEmail(emlFile);
//        System.out.println(email.getPlainText());

//		XML FILE PARSER USING DOM
        try {
            File xmlDoc = new File("C:/Users/danie/Downloads/customers.xml");
            DocumentBuilderFactory factory = DocumentBuilderFactory.newInstance();
            DocumentBuilder builder = factory.newDocumentBuilder();
            Document doc = builder.parse(xmlDoc);

//			Read root element
            System.out.println("Root element: " + doc.getDocumentElement().getNodeName());

//			Read array of customers elements
//			this array is called NodeList
            NodeList customerList = doc.getElementsByTagName("customer");

            for (int i = 0; i < customerList.getLength(); i++) {
                Node customer = customerList.item(i);
                if (customer.getNodeType() == Node.ELEMENT_NODE) {
                    Element elementCustomer = (Element) customer;

                    if (customerRepo.existsByEmail(elementCustomer.getElementsByTagName("email").item(0).getTextContent())) {
                        throw new CustomerAlreadySavedException(elementCustomer.getElementsByTagName("email").item(0).getTextContent());
                    }

                    Customer newCustomer = new Customer();
                    newCustomer.setFirstName(elementCustomer.getElementsByTagName("firstName").item(0).getTextContent());
                    newCustomer.setLastName(elementCustomer.getElementsByTagName("lastName").item(0).getTextContent());
                    newCustomer.setPhoneNumber(elementCustomer.getElementsByTagName("phoneNumber").item(0).getTextContent());
                    newCustomer.setEmail(elementCustomer.getElementsByTagName("email").item(0).getTextContent());
                    newCustomer.setCity(elementCustomer.getElementsByTagName("city").item(0).getTextContent());

                    Invoice newInvoice = new Invoice();
                    Certificate newCertificate = new Certificate();

                    customerRepo.save(newCustomer);

                    NodeList invoiceList = elementCustomer.getElementsByTagName("invoice");
                    for (int j = 0; j < invoiceList.getLength(); j++) {
                        Node invoice = invoiceList.item(j);
                        if (invoice.getNodeType() == Node.ELEMENT_NODE) {
                            Element elementInvoice = (Element) invoice;

                            if (invoiceRepo.existsByInvoiceNumber(elementInvoice.getElementsByTagName("invoiceNumber").item(0).getTextContent())) {

                                throw new InvoiceAlreadySavedException(elementInvoice.getElementsByTagName("invoiceNumber").item(0).getTextContent());
                            }

                            SimpleDateFormat simpleDateFormat = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss");
                            Date dateInvoice = simpleDateFormat.parse(elementInvoice.getElementsByTagName("dateOfAgreement").item(0).getTextContent());

                            newInvoice.setInvoiceNumber(elementInvoice.getElementsByTagName("invoiceNumber").item(0).getTextContent());
                            newInvoice.setDateOfAgreement(dateInvoice);
                            newInvoice.setStatus(elementInvoice.getElementsByTagName("status").item(0).getTextContent());

                            invoiceRepo.save(newInvoice);
                            customerService.saveInvoiceToCustomer(newCustomer.getId(), newInvoice.getId());

                            NodeList certificatesList= elementInvoice.getElementsByTagName("certificate");
                            for (int y = 0; y < certificatesList.getLength(); y++) {
                                Node certificate = certificatesList.item(y);
                                if (certificate.getNodeType() == Node.ELEMENT_NODE) {
                                    Element elementCertificate = (Element) certificate;

                                    if (certificateRepo.existsBySerialNumber(elementCertificate.getElementsByTagName("serialNumber").item(0).getTextContent())) {
                                        customerRepo.delete(newCustomer);
                                        throw new CertificateAlreadySavedException(elementCertificate.getElementsByTagName("serialNumber").item(0).getTextContent());
                                    }

                                    Date dateValidFrom = simpleDateFormat.parse(elementCertificate.getElementsByTagName("validFrom").item(0).getTextContent());
                                    Date dateValidTo = simpleDateFormat.parse(elementCertificate.getElementsByTagName("validTo").item(0).getTextContent());

                                    newCertificate.setSerialNumber(elementCertificate.getElementsByTagName("serialNumber").item(0).getTextContent());
                                    newCertificate.setValidFrom(dateValidFrom);
                                    newCertificate.setValidTo(dateValidTo);
                                    newCertificate.setCardNumber(elementCertificate.getElementsByTagName("cardNumber").item(0).getTextContent());
                                    newCertificate.setCardType(elementCertificate.getElementsByTagName("cardType").item(0).getTextContent());

                                    certificateRepo.save(newCertificate);
                                    invoiceService.saveCertificateToInvoice(newInvoice.getId(), newCertificate.getId());
                                }
                            }
                        }
                    }
                }
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
    }
}
