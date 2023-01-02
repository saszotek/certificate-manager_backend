package pl.certificatemanager.CertificateManagerApp.util;

import com.opencsv.CSVReader;
import com.opencsv.CSVReaderBuilder;
import lombok.RequiredArgsConstructor;
import org.apache.commons.io.FilenameUtils;
import org.simplejavamail.api.email.Email;
import org.simplejavamail.converter.EmailConverter;
import org.springframework.stereotype.Component;
import org.w3c.dom.Document;
import org.w3c.dom.Element;
import org.w3c.dom.Node;
import org.w3c.dom.NodeList;
import org.xml.sax.InputSource;
import pl.certificatemanager.CertificateManagerApp.exception.CertificateAlreadySavedException;
import pl.certificatemanager.CertificateManagerApp.exception.CustomerAlreadySavedException;
import pl.certificatemanager.CertificateManagerApp.exception.CustomerNotFoundByEmailException;
import pl.certificatemanager.CertificateManagerApp.exception.InvoiceAlreadySavedException;
import pl.certificatemanager.CertificateManagerApp.message.ResponseMessage;
import pl.certificatemanager.CertificateManagerApp.model.Certificate;
import pl.certificatemanager.CertificateManagerApp.model.Customer;
import pl.certificatemanager.CertificateManagerApp.model.Invoice;
import pl.certificatemanager.CertificateManagerApp.repository.CertificateRepo;
import pl.certificatemanager.CertificateManagerApp.repository.CustomerRepo;
import pl.certificatemanager.CertificateManagerApp.repository.InvoiceRepo;
import pl.certificatemanager.CertificateManagerApp.service.CustomerService;
import pl.certificatemanager.CertificateManagerApp.service.InvoiceService;

import javax.xml.parsers.DocumentBuilder;
import javax.xml.parsers.DocumentBuilderFactory;
import java.io.*;
import java.nio.file.Files;
import java.nio.file.Path;
import java.text.ParseException;
import java.text.SimpleDateFormat;
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

    public void saveFromFileToDatabase(String path) {
        try {
            String extension = FilenameUtils.getExtension(path);
            File file = new File(path);

            if (extension.equals("txt")) {
                DocumentBuilderFactory factory = DocumentBuilderFactory.newInstance();
                DocumentBuilder builder = factory.newDocumentBuilder();
                Document doc = builder.parse(file);

                if (doc.getDocumentElement().getNodeName().equals("customers")) {
                    saveCustomerFromXmlSchema(doc, path);
                } else if (doc.getDocumentElement().getNodeName().equals("invoices")) {
                    saveInvoiceFromXmlSchema(doc, path);
                }

            } else if (extension.equals("eml")) {
                InputStream inputStream = new FileInputStream(file);
                Email email = EmailConverter.emlToEmail(inputStream);
                String emailContent = email.getPlainText();

                DocumentBuilderFactory factory = DocumentBuilderFactory.newInstance();
                DocumentBuilder builder = factory.newDocumentBuilder();
                Document doc = builder.parse(new InputSource(new StringReader(emailContent)));

                inputStream.close();

                if (doc.getDocumentElement().getNodeName().equals("customers")) {
                    saveCustomerFromXmlSchema(doc, path);
                } else if (doc.getDocumentElement().getNodeName().equals("invoices")) {
                    saveInvoiceFromXmlSchema(doc, path);
                }
            } else if (extension.equals("csv")) {
                saveCustomerFromCsv(file, path);
            }
        } catch (Exception e) {
            throw new RuntimeException("Could not save data to the database. Error: " + e.getMessage());
        }
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

    private void saveCustomerFromXmlSchema(Document doc, String path) throws ParseException {
        ArrayList<Invoice> invoices = new ArrayList<>();
        ArrayList<Certificate> certificates = new ArrayList<>();
        HashMap<String, ArrayList<Certificate>> certificatesInvoice = new HashMap<>();

        NodeList customerList = doc.getElementsByTagName("customer");

        for (int i = 0; i < customerList.getLength(); i++) {
            invoices.clear();
            certificates.clear();

            Node customer = customerList.item(i);
            if (customer.getNodeType() == Node.ELEMENT_NODE) {
                Element elementCustomer = (Element) customer;

                if (customerRepo.existsByEmail(elementCustomer.getElementsByTagName("email").item(0).getTextContent())) {
                    responseMessage.setMessage("Customer with email" + elementCustomer.getElementsByTagName("email").item(0).getTextContent() + " is already saved in the database! Customers listed after weren't imported to the database due to the error.");
                    deleteFile(path);
                    throw new CustomerAlreadySavedException(elementCustomer.getElementsByTagName("email").item(0).getTextContent());
                }

                Customer newCustomer = new Customer();
                newCustomer.setFirstName(elementCustomer.getElementsByTagName("firstName").item(0).getTextContent());
                newCustomer.setLastName(elementCustomer.getElementsByTagName("lastName").item(0).getTextContent());
                newCustomer.setPhoneNumber(elementCustomer.getElementsByTagName("phoneNumber").item(0).getTextContent());
                newCustomer.setEmail(elementCustomer.getElementsByTagName("email").item(0).getTextContent());
                newCustomer.setCity(elementCustomer.getElementsByTagName("city").item(0).getTextContent());

                NodeList invoiceList = elementCustomer.getElementsByTagName("invoice");

                for (int j = 0; j < invoiceList.getLength(); j++) {
                    Node invoice = invoiceList.item(j);
                    if (invoice.getNodeType() == Node.ELEMENT_NODE) {
                        Element elementInvoice = (Element) invoice;

                        if (invoiceRepo.existsByInvoiceNumber(elementInvoice.getElementsByTagName("invoiceNumber").item(0).getTextContent())) {
                            responseMessage.setMessage("Invoice with invoice number " + elementInvoice.getElementsByTagName("invoiceNumber").item(0).getTextContent() + " is already saved in the database! Customers listed after weren't imported to the database due to the error.");
                            deleteFile(path);
                            throw new InvoiceAlreadySavedException(elementInvoice.getElementsByTagName("invoiceNumber").item(0).getTextContent());
                        }

                        SimpleDateFormat simpleDateFormat = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss");
                        Date dateInvoice = simpleDateFormat.parse(elementInvoice.getElementsByTagName("dateOfAgreement").item(0).getTextContent());

                        Invoice newInvoice = new Invoice();
                        newInvoice.setInvoiceNumber(elementInvoice.getElementsByTagName("invoiceNumber").item(0).getTextContent());
                        newInvoice.setDateOfAgreement(dateInvoice);
                        newInvoice.setStatus(elementInvoice.getElementsByTagName("status").item(0).getTextContent());

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

                                Date dateValidFrom = simpleDateFormat.parse(elementCertificate.getElementsByTagName("validFrom").item(0).getTextContent());
                                Date dateValidTo = simpleDateFormat.parse(elementCertificate.getElementsByTagName("validTo").item(0).getTextContent());

                                Certificate newCertificate = new Certificate();
                                newCertificate.setSerialNumber(elementCertificate.getElementsByTagName("serialNumber").item(0).getTextContent());
                                newCertificate.setValidFrom(dateValidFrom);
                                newCertificate.setValidTo(dateValidTo);
                                newCertificate.setCardNumber(elementCertificate.getElementsByTagName("cardNumber").item(0).getTextContent());
                                newCertificate.setCardType(elementCertificate.getElementsByTagName("cardType").item(0).getTextContent());

                                certificates.add(newCertificate);
                            }
                        }
                        certificatesInvoice.put(newInvoice.getInvoiceNumber(), certificates);
                    }
                }
                invoices.forEach(invoice -> {
                    if (certificatesInvoice.containsKey(invoice.getInvoiceNumber())) {
                        invoiceRepo.save(invoice);
                        for (Map.Entry<String, ArrayList<Certificate>> entry : certificatesInvoice.entrySet()) {
                            if (invoice.getInvoiceNumber() == entry.getKey()) {
                                ArrayList<Certificate> values = entry.getValue();
                                values.forEach(value -> {
                                    certificateRepo.save(value);
                                    invoiceService.saveCertificateToInvoice(invoice.getId(), value.getId());
                                });
                                values.clear();
                            }
                        }
                    }
                });

                customerRepo.save(newCustomer);
                invoices.forEach(invoice -> {
                    customerService.saveInvoiceToCustomer(newCustomer.getId(), invoice.getId());
                });
                invoices.clear();
                certificates.clear();
                certificatesInvoice.clear();
            }
        }
    }

    private void saveInvoiceFromXmlSchema(Document doc, String path) throws ParseException {
        ArrayList<Certificate> certificates = new ArrayList<>();

        NodeList invoiceList = doc.getElementsByTagName("invoice");

        for (int i = 0; i < invoiceList.getLength(); i++) {
            certificates.clear();

            Node invoice = invoiceList.item(i);
            if (invoice.getNodeType() == Node.ELEMENT_NODE) {
                Element elementInvoice = (Element) invoice;

                if (invoiceRepo.existsByInvoiceNumber(elementInvoice.getElementsByTagName("invoiceNumber").item(0).getTextContent())) {
                    responseMessage.setMessage("Invoice with invoice number " + elementInvoice.getElementsByTagName("invoiceNumber").item(0).getTextContent() + " is already saved in the database! Invoices listed after weren't imported to the database due to the error.");
                    deleteFile(path);
                    throw new InvoiceAlreadySavedException(elementInvoice.getElementsByTagName("invoiceNumber").item(0).getTextContent());
                }

                if (!(customerRepo.existsByEmail(elementInvoice.getElementsByTagName("email").item(0).getTextContent()))) {
                    responseMessage.setMessage("Customer with email " + elementInvoice.getElementsByTagName("email").item(0).getTextContent() + " wasn't found in the database. Invoices listed after weren't imported to the database due to the error.");
                    deleteFile(path);
                    throw new CustomerNotFoundByEmailException(elementInvoice.getElementsByTagName("email").item(0).getTextContent());
                }

                SimpleDateFormat simpleDateFormat = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss");
                Date dateInvoice = simpleDateFormat.parse(elementInvoice.getElementsByTagName("dateOfAgreement").item(0).getTextContent());

                Invoice newInvoice = new Invoice();
                newInvoice.setInvoiceNumber(elementInvoice.getElementsByTagName("invoiceNumber").item(0).getTextContent());
                newInvoice.setDateOfAgreement(dateInvoice);
                newInvoice.setStatus(elementInvoice.getElementsByTagName("status").item(0).getTextContent());

                NodeList certificatesList = elementInvoice.getElementsByTagName("certificate");

                for (int j = 0; j < certificatesList.getLength(); j++) {
                    Node certificate = certificatesList.item(j);
                    if (certificate.getNodeType() == Node.ELEMENT_NODE) {
                        Element elementCertificate = (Element) certificate;

                        if (certificateRepo.existsBySerialNumber(elementCertificate.getElementsByTagName("serialNumber").item(0).getTextContent())) {
                            responseMessage.setMessage("Certificate with serial number " + elementCertificate.getElementsByTagName("serialNumber").item(0).getTextContent() + " is already saved in the database! Invoices listed after weren't imported to the database due to the error.");
                            deleteFile(path);
                            throw new CertificateAlreadySavedException(elementCertificate.getElementsByTagName("serialNumber").item(0).getTextContent());
                        }

                        Date dateValidFrom = simpleDateFormat.parse(elementCertificate.getElementsByTagName("validFrom").item(0).getTextContent());
                        Date dateValidTo = simpleDateFormat.parse(elementCertificate.getElementsByTagName("validTo").item(0).getTextContent());

                        Certificate newCertificate = new Certificate();
                        newCertificate.setSerialNumber(elementCertificate.getElementsByTagName("serialNumber").item(0).getTextContent());
                        newCertificate.setValidFrom(dateValidFrom);
                        newCertificate.setValidTo(dateValidTo);
                        newCertificate.setCardNumber(elementCertificate.getElementsByTagName("cardNumber").item(0).getTextContent());
                        newCertificate.setCardType(elementCertificate.getElementsByTagName("cardType").item(0).getTextContent());

                        certificates.add(newCertificate);
                    }
                }
                invoiceRepo.save(newInvoice);
                certificates.forEach(certificate -> {
                    certificateRepo.save(certificate);
                    invoiceService.saveCertificateToInvoice(newInvoice.getId(), certificate.getId());
                });
                Customer customer = customerRepo.findByEmail(elementInvoice.getElementsByTagName("email").item(0).getTextContent());
                customerService.saveInvoiceToCustomer(customer.getId(), newInvoice.getId());

                certificates.clear();
            }
        }
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

                    if (certificateRepo.existsBySerialNumber(row[8])) {
                        responseMessage.setMessage("Certificate with serial number " + row[8] + " is already saved in the database! Customers listed after weren't imported to the database due to the error.");
                        deleteFile(path);
                        throw new CertificateAlreadySavedException(row[8]);
                    }

                    Customer newCustomer = new Customer();
                    newCustomer.setFirstName(row[0]);
                    newCustomer.setLastName(row[1]);
                    newCustomer.setPhoneNumber(row[2]);
                    newCustomer.setEmail(row[3]);
                    newCustomer.setCity(row[4]);

                    customerRepo.save(newCustomer);

                    SimpleDateFormat simpleDateFormat = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss");
                    Date dateInvoice = simpleDateFormat.parse(row[6]);


                    Invoice newInvoice = new Invoice();
                    newInvoice.setInvoiceNumber(row[5]);
                    newInvoice.setDateOfAgreement(dateInvoice);
                    newInvoice.setStatus(row[7]);

                    invoiceRepo.save(newInvoice);
                    customerService.saveInvoiceToCustomer(newCustomer.getId(), newInvoice.getId());

                    Date dateValidFrom = simpleDateFormat.parse(row[9]);
                    Date dateValidTo = simpleDateFormat.parse(row[10]);

                    Certificate newCertificate = new Certificate();
                    newCertificate.setSerialNumber(row[8]);
                    newCertificate.setValidFrom(dateValidFrom);
                    newCertificate.setValidTo(dateValidTo);
                    newCertificate.setCardNumber(row[11]);
                    newCertificate.setCardType(row[12]);

                    certificateRepo.save(newCertificate);
                    invoiceService.saveCertificateToInvoice(newInvoice.getId(), newCertificate.getId());
                } else {
                    if (!(invoiceRepo.existsByInvoiceNumber(row[5]))) {
                        if (certificateRepo.existsBySerialNumber(row[8])) {
                            responseMessage.setMessage("Certificate with serial number " + row[8] + " is already saved in the database! Customers listed after weren't imported to the database due to the error.");
                            deleteFile(path);
                            throw new CertificateAlreadySavedException(row[8]);
                        }

                        Customer customer = customerRepo.findByEmail(row[3]);

                        SimpleDateFormat simpleDateFormat = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss");
                        Date dateInvoice = simpleDateFormat.parse(row[6]);

                        Invoice newInvoice = new Invoice();
                        newInvoice.setInvoiceNumber(row[5]);
                        newInvoice.setDateOfAgreement(dateInvoice);
                        newInvoice.setStatus(row[7]);

                        invoiceRepo.save(newInvoice);
                        customerService.saveInvoiceToCustomer(customer.getId(), newInvoice.getId());

                        Date dateValidFrom = simpleDateFormat.parse(row[9]);
                        Date dateValidTo = simpleDateFormat.parse(row[10]);

                        Certificate newCertificate = new Certificate();
                        newCertificate.setSerialNumber(row[8]);
                        newCertificate.setValidFrom(dateValidFrom);
                        newCertificate.setValidTo(dateValidTo);
                        newCertificate.setCardNumber(row[11]);
                        newCertificate.setCardType(row[12]);

                        certificateRepo.save(newCertificate);
                        invoiceService.saveCertificateToInvoice(newInvoice.getId(), newCertificate.getId());
                    } else {
                        if (certificateRepo.existsBySerialNumber(row[8])) {
                            responseMessage.setMessage("Certificate with serial number " + row[8] + " is already saved in the database! Customers listed after weren't imported to the database due to the error.");
                            deleteFile(path);
                            throw new CertificateAlreadySavedException(row[8]);
                        }

                        Invoice invoice = invoiceRepo.findByInvoiceNumber(row[5]);

                        SimpleDateFormat simpleDateFormat = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss");
                        Date dateValidFrom = simpleDateFormat.parse(row[9]);
                        Date dateValidTo = simpleDateFormat.parse(row[10]);

                        Certificate newCertificate = new Certificate();
                        newCertificate.setSerialNumber(row[8]);
                        newCertificate.setValidFrom(dateValidFrom);
                        newCertificate.setValidTo(dateValidTo);
                        newCertificate.setCardNumber(row[11]);
                        newCertificate.setCardType(row[12]);

                        certificateRepo.save(newCertificate);
                        invoiceService.saveCertificateToInvoice(invoice.getId(), newCertificate.getId());
                    }
                }
            }
        } catch (IOException | ParseException e) {
            e.printStackTrace();
        }
    }
}
