package pl.certificatemanager.CertificateManagerApp.exception;

import lombok.extern.slf4j.Slf4j;

import java.text.MessageFormat;

@Slf4j
public class PaymentNotFoundException extends RuntimeException{
    public PaymentNotFoundException(final Long id) {
        super(MessageFormat.format("Could not find a payment info with id: {0} ", id));
        log.info("Could not find a payment info with id: {}", id);
    }
}
