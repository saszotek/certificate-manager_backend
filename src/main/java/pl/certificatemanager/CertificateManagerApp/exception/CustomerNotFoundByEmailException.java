package pl.certificatemanager.CertificateManagerApp.exception;

import lombok.extern.slf4j.Slf4j;

import java.text.MessageFormat;

@Slf4j
public class CustomerNotFoundByEmailException extends RuntimeException {
    public CustomerNotFoundByEmailException(final String email) {
        super(MessageFormat.format("Customer not found by email: {0}", email));
        log.info("Customer not found by email: {}", email);
    }
}
