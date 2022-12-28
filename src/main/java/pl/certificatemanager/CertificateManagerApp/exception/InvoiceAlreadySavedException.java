package pl.certificatemanager.CertificateManagerApp.exception;

import lombok.extern.slf4j.Slf4j;

import java.text.MessageFormat;

@Slf4j
public class InvoiceAlreadySavedException extends RuntimeException{
    public InvoiceAlreadySavedException(final String invoiceNumber) {
        super(MessageFormat.format("Invoice is already saved in the database with invoice number: {0}", invoiceNumber));
        log.info("Invoice is already saved in the database with invoice number: {}", invoiceNumber);
    }
}
