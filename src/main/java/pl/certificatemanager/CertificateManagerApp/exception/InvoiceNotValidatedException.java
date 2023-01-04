package pl.certificatemanager.CertificateManagerApp.exception;

import lombok.extern.slf4j.Slf4j;

import java.text.MessageFormat;

@Slf4j
public class InvoiceNotValidatedException extends RuntimeException{
    public InvoiceNotValidatedException(final String invoiceNumber) {
        super(MessageFormat.format("Invoice with invoice number {0} could not be added, because of incorrect data", invoiceNumber));
        log.info("Invoice with invoice number {} could not be added, because of wrong data format", invoiceNumber);
    }
}
