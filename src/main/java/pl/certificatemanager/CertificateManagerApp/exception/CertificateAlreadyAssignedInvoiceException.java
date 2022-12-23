package pl.certificatemanager.CertificateManagerApp.exception;

import lombok.extern.slf4j.Slf4j;

import java.text.MessageFormat;

@Slf4j
public class CertificateAlreadyAssignedInvoiceException extends RuntimeException {
    public CertificateAlreadyAssignedInvoiceException(final Long certificateId, final Long invoiceId) {
        super(MessageFormat.format("Certificate: {0} is already assigned to invoice: {1}", certificateId, invoiceId));
        log.info("Certificate: {} is already assigned to invoice: {}", certificateId, invoiceId);
    }
}
