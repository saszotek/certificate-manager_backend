package pl.certificatemanager.CertificateManagerApp.exception;

import lombok.extern.slf4j.Slf4j;

import java.text.MessageFormat;

@Slf4j
public class InvoiceAlreadyAssignedCustomerException extends RuntimeException {
    public InvoiceAlreadyAssignedCustomerException(final Long invoiceId, final Long customerId) {
        super(MessageFormat.format("Invoice: {0} is already assigned to customer: {1}", invoiceId, customerId));
        log.info("Invoice: {} is already assigned to customer: {}", invoiceId, customerId);
    }
}
