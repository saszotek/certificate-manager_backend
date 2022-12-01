package pl.certificatemanager.CertificateManagerApp.service;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import pl.certificatemanager.CertificateManagerApp.exception.PaymentNotFoundException;
import pl.certificatemanager.CertificateManagerApp.model.Payment;
import pl.certificatemanager.CertificateManagerApp.repository.PaymentRepo;

import javax.transaction.Transactional;
import java.util.List;

@Service
@RequiredArgsConstructor
@Transactional
@Slf4j
public class PaymentService {
    private final PaymentRepo paymentRepo;

    public List<Payment> getAllPayments() {
        log.info("Fetching all payments info");
        return paymentRepo.findAll();
    }

    public Payment getPaymentById(Long id) {
        return paymentRepo.findById(id).orElseThrow(() -> new PaymentNotFoundException(id));
    }

    public Payment savePayment(Payment payment){
        log.info("Saving new payment info {} to the database", payment.getAccountNumber());
        return paymentRepo.save(payment);
    }

    public Payment deletePayment(Long id) {
        Payment payment = getPaymentById(id);
        log.info("Deleting payment info {}", payment);
        paymentRepo.delete(payment);
        return payment;
    }
}
