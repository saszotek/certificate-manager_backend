package pl.certificatemanager.CertificateManagerApp.exception;

import lombok.extern.slf4j.Slf4j;

import java.text.MessageFormat;

@Slf4j
public class CertificateAlreadySavedException extends RuntimeException{
    public CertificateAlreadySavedException(final String serialNumber) {
        super(MessageFormat.format("Certificate is already saved in the database with serial number: {0}", serialNumber));
        log.info("Certificate is already saved in the database with serial number: {}", serialNumber);
    }
}
