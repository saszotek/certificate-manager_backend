package pl.certificatemanager.CertificateManagerApp.util;

import com.google.common.collect.LinkedHashMultimap;
import com.google.common.collect.Multimap;
import com.opencsv.CSVReader;
import com.opencsv.CSVReaderBuilder;
import lombok.RequiredArgsConstructor;
import org.apache.commons.io.FilenameUtils;
import org.simplejavamail.api.email.AttachmentResource;
import org.simplejavamail.api.email.Email;
import org.simplejavamail.converter.EmailConverter;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;
import org.w3c.dom.Document;
import org.w3c.dom.Element;
import org.w3c.dom.Node;
import org.w3c.dom.NodeList;
import org.xml.sax.InputSource;
import pl.certificatemanager.CertificateManagerApp.exception.*;
import pl.certificatemanager.CertificateManagerApp.message.ResponseMessage;
import pl.certificatemanager.CertificateManagerApp.model.Certificate;
import pl.certificatemanager.CertificateManagerApp.model.Customer;
import pl.certificatemanager.CertificateManagerApp.model.Invoice;
import pl.certificatemanager.CertificateManagerApp.payload.EmailRequest;
import pl.certificatemanager.CertificateManagerApp.repository.CertificateRepo;
import pl.certificatemanager.CertificateManagerApp.repository.CustomerRepo;
import pl.certificatemanager.CertificateManagerApp.repository.InvoiceRepo;
import pl.certificatemanager.CertificateManagerApp.service.CustomerService;
import pl.certificatemanager.CertificateManagerApp.service.InvoiceService;
import pl.certificatemanager.CertificateManagerApp.service.SchedulerService;

import javax.xml.parsers.DocumentBuilder;
import javax.xml.parsers.DocumentBuilderFactory;
import javax.xml.transform.Transformer;
import javax.xml.transform.TransformerFactory;
import javax.xml.transform.dom.DOMSource;
import javax.xml.transform.stream.StreamResult;
import java.io.*;
import java.nio.file.Files;
import java.nio.file.Path;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.time.LocalDateTime;
import java.time.ZoneId;
import java.util.*;

@Component
@RequiredArgsConstructor
public class FilesUtil {
    private final CustomerRepo customerRepo;
    private final InvoiceRepo invoiceRepo;
    private final CertificateRepo certificateRepo;
    private final CustomerService customerService;
    private final InvoiceService invoiceService;
    private final ResponseMessage responseMessage;
    private final SchedulerService schedulerService;

    @Value("${filesManagement.path}")
    private String directoryPath;

    public void saveFromFileToDatabase(String path) {
        try {
            String extension = FilenameUtils.getExtension(path);
            File file = new File(path);

            if (extension.equals("eml")) {
                InputStream inputStream = new FileInputStream(file);
                Email email = EmailConverter.emlToEmail(inputStream);
                List<AttachmentResource> attachments = email.getAttachments();
                inputStream.close();

                if (!(attachments.isEmpty())) {
                    attachments.forEach(attachment -> {
                        String filePathAttachment = directoryPath + attachment.getName();

                        try {
                            Files.copy(attachment.getDataSourceInputStream(), Path.of(filePathAttachment));
                            String extensionAttachment = FilenameUtils.getExtension(filePathAttachment);
                            File fileAttachment = new File(filePathAttachment);

                            parseFileBasedOnExtension(extensionAttachment, filePathAttachment, fileAttachment);
                            deleteFile(filePathAttachment);
                        } catch (Exception e) {
                            throw new RuntimeException("Could not store attachment from .eml file! Error: " + e.getMessage());
                        }
                    });
                } else {
                    responseMessage.setMessage("Wrong file type! Use only .txt, .csv, .eml with correctly formatted data.");
                    deleteFile(path);
                    throw new RuntimeException(responseMessage.getMessage());
                }
            } else {
                parseFileBasedOnExtension(extension, path, file);
            }
        } catch (Exception e) {
            throw new RuntimeException("Could not save data to the database. Error: " + e.getMessage());
        }
    }

    public File saveFromDatabaseToFile(String typeOfExport) {
        if (typeOfExport.equals("customers")) {
            return saveCustomersToXml();
        }

        throw new RuntimeException("Could not determine what export to make!");
    }

    public void deleteFile(String path) {
        try {
            boolean result = Files.deleteIfExists(Path.of(path));
            if (result) {
                System.out.println("File is deleted!");
            } else {
                System.out.println("Sorry, unable to delete the file.");
            }
        } catch (IOException e) {
            throw new RuntimeException("Something went wrong with deleting your file. Error: " + e.getMessage());
        }
    }

    private void parseFileBasedOnExtension(String extension, String path, File file) {
        try {
            if (extension.equals("txt")) {
                DocumentBuilderFactory factory = DocumentBuilderFactory.newInstance();
                DocumentBuilder builder = factory.newDocumentBuilder();
                Document doc = builder.parse(file);

                saveCustomerFromXmlSchema(doc, path);
            } else if (extension.equals("eml")) {
                InputStream inputStream = new FileInputStream(file);
                Email email = EmailConverter.emlToEmail(inputStream);
                String emailContent = email.getPlainText();

                DocumentBuilderFactory factory = DocumentBuilderFactory.newInstance();
                DocumentBuilder builder = factory.newDocumentBuilder();
                Document doc = builder.parse(new InputSource(new StringReader(emailContent)));

                inputStream.close();

                saveCustomerFromXmlSchema(doc, path);
            } else if (extension.equals("csv")) {
                saveCustomerFromCsv(file, path);
            }
        } catch (Exception e) {
            throw new RuntimeException("Something went wrong! Could not save data to the database. Error: " + e.getMessage());
        }
    }

    private void saveCustomerFromXmlSchema(Document doc, String path) throws ParseException {
        ArrayList<Invoice> invoices = new ArrayList<>();
        Multimap<String, Certificate> map = LinkedHashMultimap.create();

        NodeList customerList = doc.getElementsByTagName("customer");

        for (int i = 0; i < customerList.getLength(); i++) {
            Node customer = customerList.item(i);

            if (customer.getNodeType() == Node.ELEMENT_NODE) {
                Element elementCustomer = (Element) customer;

                if (customerRepo.existsByEmail(elementCustomer.getElementsByTagName("email").item(0).getTextContent())) {
                    parseInvoicesAndItsCertificates(path, elementCustomer, invoices, map, elementCustomer.getElementsByTagName("email").item(0).getTextContent());

                    Customer oldCustomer = customerRepo.findByEmail(elementCustomer.getElementsByTagName("email").item(0).getTextContent());
                    invoices.forEach(invoice -> {
                        customerService.saveInvoiceToCustomer(oldCustomer.getId(), invoice.getId());
                    });
                } else {
                    String firstName = elementCustomer.getElementsByTagName("firstName").item(0).getTextContent();
                    String lastName = elementCustomer.getElementsByTagName("lastName").item(0).getTextContent();
                    String phoneNumber = elementCustomer.getElementsByTagName("phoneNumber").item(0).getTextContent();
                    String email = elementCustomer.getElementsByTagName("email").item(0).getTextContent();
                    String city = elementCustomer.getElementsByTagName("city").item(0).getTextContent();

                    if (!(validateCustomer(firstName, lastName, phoneNumber, email, city))) {
                        deleteFile(path);
                        throw new CustomerNotValidatedException(email);
                    }

                    Customer newCustomer = new Customer();
                    newCustomer.setFirstName(firstName);
                    newCustomer.setLastName(lastName);
                    newCustomer.setPhoneNumber(phoneNumber);
                    newCustomer.setEmail(email);
                    newCustomer.setCity(city);

                    parseInvoicesAndItsCertificates(path, elementCustomer, invoices, map, elementCustomer.getElementsByTagName("email").item(0).getTextContent());

                    customerRepo.save(newCustomer);
                    invoices.forEach(invoice -> {
                        customerService.saveInvoiceToCustomer(newCustomer.getId(), invoice.getId());
                    });
                }
                invoices.clear();
                map.clear();
            }
        }
    }

    private void parseInvoicesAndItsCertificates(String path, Element elementCustomer, ArrayList<Invoice> invoices, Multimap<String, Certificate> map, String email) throws ParseException {
        NodeList invoiceList = elementCustomer.getElementsByTagName("invoice");

        for (int j = 0; j < invoiceList.getLength(); j++) {
            Node invoice = invoiceList.item(j);

            if (invoice.getNodeType() == Node.ELEMENT_NODE) {
                Element elementInvoice = (Element) invoice;

                if (invoiceRepo.existsByInvoiceNumber(elementInvoice.getElementsByTagName("invoiceNumber").item(0).getTextContent())) {
                    responseMessage.setMessage("Invoice with invoice number " + elementInvoice.getElementsByTagName("invoiceNumber").item(0).getTextContent() + " associated with email " + elementCustomer.getElementsByTagName("email").item(0).getTextContent() + " is already saved in the database! Customers listed after weren't imported to the database due to the error.");
                    deleteFile(path);
                    throw new InvoiceAlreadySavedException(elementInvoice.getElementsByTagName("invoiceNumber").item(0).getTextContent());
                }

                String invoiceNumber = elementInvoice.getElementsByTagName("invoiceNumber").item(0).getTextContent();
                String dateOfAgreement = elementInvoice.getElementsByTagName("dateOfAgreement").item(0).getTextContent();

                if (!(validateInvoice(invoiceNumber, dateOfAgreement, path))) {
                    deleteFile(path);
                    throw new InvoiceNotValidatedException(invoiceNumber);
                }

                SimpleDateFormat simpleDateFormat = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss z");
                Date dateInvoice = simpleDateFormat.parse(dateOfAgreement);

                Invoice newInvoice = new Invoice();
                newInvoice.setInvoiceNumber(invoiceNumber);
                newInvoice.setDateOfAgreement(dateInvoice);

                invoices.add(newInvoice);

                NodeList certificatesList = elementInvoice.getElementsByTagName("certificate");

                for (int y = 0; y < certificatesList.getLength(); y++) {
                    Node certificate = certificatesList.item(y);

                    if (certificate.getNodeType() == Node.ELEMENT_NODE) {
                        Element elementCertificate = (Element) certificate;

                        if (certificateRepo.existsBySerialNumber(elementCertificate.getElementsByTagName("serialNumber").item(0).getTextContent())) {
                            responseMessage.setMessage("Certificate with serial number " + elementCertificate.getElementsByTagName("serialNumber").item(0).getTextContent() + " is already saved in the database! Customers listed after weren't imported to the database due to the error.");
                            deleteFile(path);
                            throw new CertificateAlreadySavedException(elementCertificate.getElementsByTagName("serialNumber").item(0).getTextContent());
                        }

                        String serialNumber = elementCertificate.getElementsByTagName("serialNumber").item(0).getTextContent();
                        String validFrom = elementCertificate.getElementsByTagName("validFrom").item(0).getTextContent();
                        String validTo = elementCertificate.getElementsByTagName("validTo").item(0).getTextContent();
                        String cardNumber = elementCertificate.getElementsByTagName("cardNumber").item(0).getTextContent();
                        String cardType = elementCertificate.getElementsByTagName("cardType").item(0).getTextContent();
                        String status = elementCertificate.getElementsByTagName("status").item(0).getTextContent();

                        if (!(validateCertificate(serialNumber, validFrom, validTo, cardNumber, cardType, status, path))) {
                            deleteFile(path);
                            throw new CertificateNotValidatedException(serialNumber);
                        }

                        Date dateValidFrom = simpleDateFormat.parse(validFrom);
                        Date dateValidTo = simpleDateFormat.parse(validTo);

                        Certificate newCertificate = new Certificate();
                        newCertificate.setSerialNumber(serialNumber);
                        newCertificate.setValidFrom(dateValidFrom);
                        newCertificate.setValidTo(dateValidTo);
                        newCertificate.setCardNumber(cardNumber);
                        newCertificate.setCardType(cardType);
                        newCertificate.setStatus(status);

                        map.put(newInvoice.getInvoiceNumber(), newCertificate);
                    }
                }
            }
        }
        invoices.forEach(invoice -> {
            if (map.containsKey(invoice.getInvoiceNumber())) {
                invoiceRepo.save(invoice);
                map.get(invoice.getInvoiceNumber()).forEach(certificate -> {
                    certificateRepo.save(certificate);
                    invoiceService.saveCertificateToInvoice(invoice.getId(), certificate.getId());

                    schedulerService.setupEmailSchedule(email, certificate.getSerialNumber(), invoice.getInvoiceNumber(), certificate.getValidTo(), 60);
                    schedulerService.setupEmailSchedule(email, certificate.getSerialNumber(), invoice.getInvoiceNumber(), certificate.getValidTo(), 30);
                    schedulerService.setupEmailSchedule(email, certificate.getSerialNumber(), invoice.getInvoiceNumber(), certificate.getValidTo(), 14);
                    schedulerService.setupEmailSchedule(email, certificate.getSerialNumber(), invoice.getInvoiceNumber(), certificate.getValidTo(), 7);
                });
            }
        });
    }

    private void saveCustomerFromCsv(File file, String path) {
        try {
            FileReader fileReader = new FileReader(file);
            CSVReader csvReader = new CSVReaderBuilder(fileReader).withSkipLines(1).build();
            List<String[]> allData = csvReader.readAll();
            fileReader.close();

            for (String[] row : allData) {
                if (!(customerRepo.existsByEmail(row[3]))) {
                    if (invoiceRepo.existsByInvoiceNumber(row[5])) {
                        responseMessage.setMessage("Invoice with invoice number " + row[5] + " is already saved in the database! Customers listed after weren't imported to the database due to the error.");
                        deleteFile(path);
                        throw new InvoiceAlreadySavedException(row[5]);

                    }

                    if (certificateRepo.existsBySerialNumber(row[7])) {
                        responseMessage.setMessage("Certificate with serial number " + row[7] + " is already saved in the database! Customers listed after weren't imported to the database due to the error.");
                        deleteFile(path);
                        throw new CertificateAlreadySavedException(row[7]);
                    }
                    String firstName = row[0];
                    String lastName = row[1];
                    String phoneNumber = row[2];
                    String email = row[3];
                    String city = row[4];

                    if (!(validateCustomer(firstName, lastName, phoneNumber, email, city))) {
                        deleteFile(path);
                        throw new CustomerNotValidatedException(email);
                    }

                    String invoiceNumber = row[5];
                    String dateOfAgreement = row[6];

                    if (!(validateInvoice(invoiceNumber, dateOfAgreement, path))) {
                        deleteFile(path);
                        throw new InvoiceNotValidatedException(invoiceNumber);
                    }

                    String serialNumber = row[7];
                    String validFrom = row[8];
                    String validTo = row[9];
                    String cardNumber = row[10];
                    String cardType = row[11];
                    String status = row[12];

                    if (!(validateCertificate(serialNumber, validFrom, validTo, cardNumber, cardType, status, path))) {
                        deleteFile(path);
                        throw new CertificateNotValidatedException(serialNumber);
                    }

                    Customer newCustomer = new Customer();
                    newCustomer.setFirstName(firstName);
                    newCustomer.setLastName(lastName);
                    newCustomer.setPhoneNumber(phoneNumber);
                    newCustomer.setEmail(email);
                    newCustomer.setCity(city);

                    customerRepo.save(newCustomer);

                    SimpleDateFormat simpleDateFormat = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss z");
                    Date dateInvoice = simpleDateFormat.parse(dateOfAgreement);


                    Invoice newInvoice = new Invoice();
                    newInvoice.setInvoiceNumber(invoiceNumber);
                    newInvoice.setDateOfAgreement(dateInvoice);

                    invoiceRepo.save(newInvoice);
                    customerService.saveInvoiceToCustomer(newCustomer.getId(), newInvoice.getId());

                    Date dateValidFrom = simpleDateFormat.parse(validFrom);
                    Date dateValidTo = simpleDateFormat.parse(validTo);

                    Certificate newCertificate = new Certificate();
                    newCertificate.setSerialNumber(serialNumber);
                    newCertificate.setValidFrom(dateValidFrom);
                    newCertificate.setValidTo(dateValidTo);
                    newCertificate.setCardNumber(cardNumber);
                    newCertificate.setCardType(cardType);
                    newCertificate.setStatus(status);

                    certificateRepo.save(newCertificate);
                    invoiceService.saveCertificateToInvoice(newInvoice.getId(), newCertificate.getId());

                    schedulerService.setupEmailSchedule(email, serialNumber, invoiceNumber, dateValidTo, 60);
                    schedulerService.setupEmailSchedule(email, serialNumber, invoiceNumber, dateValidTo, 14);
                    schedulerService.setupEmailSchedule(email, serialNumber, invoiceNumber, dateValidTo, 30);
                    schedulerService.setupEmailSchedule(email, serialNumber, invoiceNumber, dateValidTo, 7);
                } else {
                    if (!(invoiceRepo.existsByInvoiceNumber(row[5]))) {
                        if (certificateRepo.existsBySerialNumber(row[7])) {
                            responseMessage.setMessage("Certificate with serial number " + row[7] + " is already saved in the database! Customers listed after weren't imported to the database due to the error.");
                            deleteFile(path);
                            throw new CertificateAlreadySavedException(row[7]);
                        }

                        String invoiceNumber = row[5];
                        String dateOfAgreement = row[6];

                        if (!(validateInvoice(invoiceNumber, dateOfAgreement, path))) {
                            deleteFile(path);
                            throw new InvoiceNotValidatedException(invoiceNumber);
                        }

                        String serialNumber = row[7];
                        String validFrom = row[8];
                        String validTo = row[9];
                        String cardNumber = row[10];
                        String cardType = row[11];
                        String status = row[12];

                        if (!(validateCertificate(serialNumber, validFrom, validTo, cardNumber, cardType, status, path))) {
                            deleteFile(path);
                            throw new CertificateNotValidatedException(serialNumber);
                        }

                        Customer customer = customerRepo.findByEmail(row[3]);

                        SimpleDateFormat simpleDateFormat = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss z");
                        Date dateInvoice = simpleDateFormat.parse(dateOfAgreement);

                        Invoice newInvoice = new Invoice();
                        newInvoice.setInvoiceNumber(invoiceNumber);
                        newInvoice.setDateOfAgreement(dateInvoice);

                        invoiceRepo.save(newInvoice);
                        customerService.saveInvoiceToCustomer(customer.getId(), newInvoice.getId());

                        Date dateValidFrom = simpleDateFormat.parse(validFrom);
                        Date dateValidTo = simpleDateFormat.parse(validTo);

                        Certificate newCertificate = new Certificate();
                        newCertificate.setSerialNumber(serialNumber);
                        newCertificate.setValidFrom(dateValidFrom);
                        newCertificate.setValidTo(dateValidTo);
                        newCertificate.setCardNumber(cardNumber);
                        newCertificate.setCardType(cardType);
                        newCertificate.setStatus(status);

                        certificateRepo.save(newCertificate);
                        invoiceService.saveCertificateToInvoice(newInvoice.getId(), newCertificate.getId());

                        String email = row[3];

                        schedulerService.setupEmailSchedule(email, serialNumber, invoiceNumber, dateValidTo, 60);
                        schedulerService.setupEmailSchedule(email, serialNumber, invoiceNumber, dateValidTo, 30);
                        schedulerService.setupEmailSchedule(email, serialNumber, invoiceNumber, dateValidTo, 14);
                        schedulerService.setupEmailSchedule(email, serialNumber, invoiceNumber, dateValidTo, 7);
                    } else {
                        if (certificateRepo.existsBySerialNumber(row[7])) {
                            responseMessage.setMessage("Certificate with serial number " + row[7] + " is already saved in the database! Customers listed after weren't imported to the database due to the error.");
                            deleteFile(path);
                            throw new CertificateAlreadySavedException(row[7]);
                        }

                        String serialNumber = row[7];
                        String validFrom = row[8];
                        String validTo = row[9];
                        String cardNumber = row[10];
                        String cardType = row[11];
                        String status = row[12];

                        if (!(validateCertificate(serialNumber, validFrom, validTo, cardNumber, cardType, status, path))) {
                            deleteFile(path);
                            throw new CertificateNotValidatedException(serialNumber);
                        }

                        Invoice invoice = invoiceRepo.findByInvoiceNumber(row[5]);

                        SimpleDateFormat simpleDateFormat = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss z");
                        Date dateValidFrom = simpleDateFormat.parse(validFrom);
                        Date dateValidTo = simpleDateFormat.parse(validTo);

                        Certificate newCertificate = new Certificate();
                        newCertificate.setSerialNumber(serialNumber);
                        newCertificate.setValidFrom(dateValidFrom);
                        newCertificate.setValidTo(dateValidTo);
                        newCertificate.setCardNumber(cardNumber);
                        newCertificate.setCardType(cardType);
                        newCertificate.setStatus(status);

                        certificateRepo.save(newCertificate);
                        invoiceService.saveCertificateToInvoice(invoice.getId(), newCertificate.getId());

                        String email = row[3];

                        schedulerService.setupEmailSchedule(email, serialNumber, invoice.getInvoiceNumber(), dateValidTo, 60);
                        schedulerService.setupEmailSchedule(email, serialNumber, invoice.getInvoiceNumber(), dateValidTo, 30);
                        schedulerService.setupEmailSchedule(email, serialNumber, invoice.getInvoiceNumber(), dateValidTo, 14);
                        schedulerService.setupEmailSchedule(email, serialNumber, invoice.getInvoiceNumber(), dateValidTo, 7);
                    }
                }
            }
        } catch (IOException | ParseException e) {
            e.printStackTrace();
        }
    }

    private Boolean validateCustomer(String firstName, String lastName, String phoneNumber, String email, String city) {
        String phoneRegex = "(?<!\\d)\\d{9}(?!\\d)";
        String emailRegex = "(?:[a-z0-9!#$%&'*+/=?^_`{|}~-]+(?:\\.[a-z0-9!#$%&'*+/=?^_`{|}~-]+)*|\"(?:[\\x01-\\x08\\x0b\\x0c\\x0e-\\x1f\\x21\\x23-\\x5b\\x5d-\\x7f]|\\\\[\\x01-\\x09\\x0b\\x0c\\x0e-\\x7f])*\")@(?:(?:[a-z0-9](?:[a-z0-9-]*[a-z0-9])?\\.)+[a-z0-9](?:[a-z0-9-]*[a-z0-9])?|\\[(?:(?:(2(5[0-5]|[0-4][0-9])|1[0-9][0-9]|[1-9]?[0-9]))\\.){3}(?:(2(5[0-5]|[0-4][0-9])|1[0-9][0-9]|[1-9]?[0-9])|[a-z0-9-]*[a-z0-9]:(?:[\\x01-\\x08\\x0b\\x0c\\x0e-\\x1f\\x21-\\x5a\\x53-\\x7f]|\\\\[\\x01-\\x09\\x0b\\x0c\\x0e-\\x7f])+)\\])";

        if (firstName.isEmpty()) {
            responseMessage.setMessage("Customer with email " + email + " could not be added, because of incorrect data in first name field! Customers listed after weren't imported to the database due to the error.");
            return false;
        }

        if (lastName.isEmpty()) {
            responseMessage.setMessage("Customer with email " + email + " could not be added, because of incorrect data in last name field! Customers listed after weren't imported to the database due to the error.");
            return false;
        }

        if (city.isEmpty()) {
            responseMessage.setMessage("Customer with email " + email + " could not be added, because of incorrect data in city field! Customers listed after weren't imported to the database due to the error.");
            return false;
        }

        if (!(phoneNumber.matches(phoneRegex))) {
            responseMessage.setMessage("Customer with email " + email + " could not be added, because of incorrect data in phone number field! Customers listed after weren't imported to the database due to the error.");
            return false;
        }

        if (!(email.matches(emailRegex))) {
            responseMessage.setMessage("Customer with email " + email + " could not be added, because of incorrect data in email field! Customers listed after weren't imported to the database due to the error.");
            return false;
        }

        return true;
    }

    private Boolean validateInvoice(String invoiceNumber, String dateOfAgreement, String path) {
        String invoiceRegex = "[0-9]+";
        SimpleDateFormat simpleDateFormat = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss z");
        simpleDateFormat.setLenient(true);

        if (!(invoiceNumber.matches(invoiceRegex)) || invoiceNumber.isEmpty()) {
            responseMessage.setMessage("Invoice with invoice number " + invoiceNumber + " could not be added, because of incorrect data in invoice number field! Customers listed after weren't imported to the database due to the error.");
            return false;
        }

        try {
            simpleDateFormat.parse(dateOfAgreement);
        } catch (ParseException e) {
            responseMessage.setMessage("Invoice with invoice number " + invoiceNumber + " could not be added, because of incorrect data in date of agreement field! Example of proper date format: 1970-01-25 19:06:00 GMT. Customers listed after weren't imported to the database due to the error.");
            deleteFile(path);
            throw new InvoiceNotValidatedException(invoiceNumber);
        }

        return true;
    }

    private Boolean validateCertificate(String serialNumber, String validFrom, String validTo, String cardNumber, String cardType, String status, String path) {
        String cardRegex = "[0-9]+";
        SimpleDateFormat simpleDateFormat = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss z");
        simpleDateFormat.setLenient(true);

        if (serialNumber.isEmpty()) {
            responseMessage.setMessage("Certificate with serial number " + serialNumber + " could not be added, because of incorrect data in serial number field! Customers listed after weren't imported to the database due to the error.");
            return false;
        }

        if (!(cardNumber.matches(cardRegex)) || cardNumber.isEmpty()) {
            responseMessage.setMessage("Certificate with serial number " + serialNumber + " could not be added, because of incorrect data in card number field! Customers listed after weren't imported to the database due to the error.");
            return false;
        }

        if (cardType.isEmpty()) {
            responseMessage.setMessage("Certificate with serial number " + serialNumber + " could not be added, because of incorrect data in card type field! Customers listed after weren't imported to the database due to the error.");
            return false;
        }

        if (!(status.equals("Invoice sent")) && !(status.equals("Expired")) && !(status.equals("Resigned")) && !(status.equals("Completed")) && !(status.equals("At the competition")) && !(status.equals("Paid"))) {
            responseMessage.setMessage("Certificate with serial number " + serialNumber + " could not be added, because of incorrect data in status field! Use one of these: Invoice sent, Expired, Resigned, Completed, At the competition or Paid. Customers listed after weren't imported to the database due to the error.");
            return false;
        }

        try {
            simpleDateFormat.parse(validFrom);
            simpleDateFormat.parse(validTo);
        } catch (ParseException e) {
            responseMessage.setMessage("Certificate with serial number " + serialNumber + " could not be added, because of incorrect data in valid from or valid to fields! Example of proper date format: 1970-01-25 19:06:00 GMT. Customers listed after weren't imported to the database due to the error.");
            deleteFile(path);
            throw new CertificateNotValidatedException(serialNumber);
        }

        return true;
    }

    private File saveCustomersToXml() {
        try {
            DocumentBuilderFactory factory = DocumentBuilderFactory.newInstance();
            DocumentBuilder builder = factory.newDocumentBuilder();
            Document doc = builder.newDocument();

            Element rootElement = doc.createElement("customers");
            doc.appendChild(rootElement);

            SimpleDateFormat simpleDateFormat = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss z");
            List<Customer> customers = customerRepo.findAll();

            customers.forEach(customer -> {
                Element customerElement = doc.createElement("customer");
                rootElement.appendChild(customerElement);

                Element firstName = doc.createElement("firstName");
                firstName.appendChild(doc.createTextNode(customer.getFirstName()));
                customerElement.appendChild(firstName);

                Element lastName = doc.createElement("lastName");
                lastName.appendChild(doc.createTextNode(customer.getLastName()));
                customerElement.appendChild(lastName);

                Element phoneNumber = doc.createElement("phoneNumber");
                phoneNumber.appendChild(doc.createTextNode(customer.getPhoneNumber()));
                customerElement.appendChild(phoneNumber);

                Element email = doc.createElement("email");
                email.appendChild(doc.createTextNode(customer.getEmail()));
                customerElement.appendChild(email);

                Element city = doc.createElement("city");
                city.appendChild(doc.createTextNode(customer.getCity()));
                customerElement.appendChild(city);

                Element invoicesList = doc.createElement("invoices");
                customerElement.appendChild(invoicesList);

                List<Invoice> invoices = invoiceRepo.findInvoicesByCustomer(customer);

                invoices.forEach(invoice -> {
                    Element invoiceList = doc.createElement("invoice");
                    invoicesList.appendChild(invoiceList);

                    Element invoiceNumber = doc.createElement("invoiceNumber");
                    invoiceNumber.appendChild(doc.createTextNode(invoice.getInvoiceNumber()));
                    invoiceList.appendChild(invoiceNumber);

                    Element dateOfAgreement = doc.createElement("dateOfAgreement");
                    dateOfAgreement.appendChild(doc.createTextNode(simpleDateFormat.format(invoice.getDateOfAgreement())));
                    invoiceList.appendChild(dateOfAgreement);

                    Element certificatesElement = doc.createElement("certificates");
                    invoiceList.appendChild(certificatesElement);

                    List<Certificate> certificates = certificateRepo.findCertificatesByInvoice(invoice);

                    certificates.forEach(certificate -> {
                        Element certificateElement = doc.createElement("certificate");
                        certificatesElement.appendChild(certificateElement);

                        Element serialNumber = doc.createElement("serialNumber");
                        serialNumber.appendChild(doc.createTextNode(certificate.getSerialNumber()));
                        certificateElement.appendChild(serialNumber);

                        Element validFrom = doc.createElement("validFrom");
                        validFrom.appendChild(doc.createTextNode(simpleDateFormat.format(certificate.getValidFrom())));
                        certificateElement.appendChild(validFrom);

                        Element validTo = doc.createElement("validTo");
                        validTo.appendChild(doc.createTextNode(simpleDateFormat.format(certificate.getValidTo())));
                        certificateElement.appendChild(validTo);

                        Element cardNumber = doc.createElement("cardNumber");
                        cardNumber.appendChild(doc.createTextNode(certificate.getCardNumber()));
                        certificateElement.appendChild(cardNumber);

                        Element cardType = doc.createElement("cardType");
                        cardType.appendChild(doc.createTextNode(certificate.getCardType()));
                        certificateElement.appendChild(cardType);

                        Element status = doc.createElement("status");
                        status.appendChild(doc.createTextNode(certificate.getStatus()));
                        certificateElement.appendChild(status);
                    });
                });
            });

            TransformerFactory transformerFactory = TransformerFactory.newInstance();
            Transformer transformer = transformerFactory.newTransformer();
            DOMSource source = new DOMSource(doc);
            StreamResult result = new StreamResult(new File(directoryPath + "exported_customers.txt"));
            transformer.transform(source, result);

            File file = new File(directoryPath + "exported_customers.txt");

            return file;
        } catch (Exception e) {
            throw new RuntimeException("Could not create xml file with customers! Error: " + e.getMessage());
        }
    }
}
