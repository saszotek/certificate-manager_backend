package pl.certificatemanager.CertificateManagerApp.exception;

import lombok.extern.slf4j.Slf4j;

import java.text.MessageFormat;

@Slf4j
public class CustomerNotValidatedException extends RuntimeException{
    public CustomerNotValidatedException(final String email) {
        super(MessageFormat.format("Customer with email {0} could not be added, because of incorrect data", email));
        log.info("Customer with email {} could not be added, because of wrong data format", email);
    }
}
