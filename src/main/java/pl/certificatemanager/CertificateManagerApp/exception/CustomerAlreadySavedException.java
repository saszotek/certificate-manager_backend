package pl.certificatemanager.CertificateManagerApp.exception;

import lombok.extern.slf4j.Slf4j;

import java.text.MessageFormat;

@Slf4j
public class CustomerAlreadySavedException extends RuntimeException{
    public CustomerAlreadySavedException(final String email) {
        super(MessageFormat.format("Customer is already saved in the database with email: {0}", email));
        log.info("Customer is already saved in the database with email: {}", email);
    }
}
