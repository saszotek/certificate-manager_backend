package pl.certificatemanager.CertificateManagerApp.exception;

import lombok.extern.slf4j.Slf4j;

import java.text.MessageFormat;

@Slf4j
public class CertificateNotValidatedException extends RuntimeException{
    public CertificateNotValidatedException(final String serialNumber) {
        super(MessageFormat.format("Certificate with serial number {0} could not be added, because of incorrect data", serialNumber));
        log.info("Certificate with serial number {} could not be added, because of wrong data format", serialNumber);
    }
}
