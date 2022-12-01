package pl.certificatemanager.CertificateManagerApp.exception;

import lombok.extern.slf4j.Slf4j;

import java.text.MessageFormat;

@Slf4j
public class PaymentAlreadyAssignedPersonException extends RuntimeException{
    public PaymentAlreadyAssignedPersonException(final Long paymentId, final Long personId) {
        super(MessageFormat.format("Payment info: {0} is already assigned to person: {1}", paymentId, personId));
        log.info("Payment info: {} is already assigned to person: {}", paymentId, personId);
    }
}
