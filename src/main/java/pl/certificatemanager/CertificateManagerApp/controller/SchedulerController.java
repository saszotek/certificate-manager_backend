package pl.certificatemanager.CertificateManagerApp.controller;

import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import pl.certificatemanager.CertificateManagerApp.model.Certificate;
import pl.certificatemanager.CertificateManagerApp.model.Customer;
import pl.certificatemanager.CertificateManagerApp.model.Invoice;
import pl.certificatemanager.CertificateManagerApp.payload.EmailRequest;
import pl.certificatemanager.CertificateManagerApp.payload.EmailResponse;
import pl.certificatemanager.CertificateManagerApp.payload.UserEmailRequest;
import pl.certificatemanager.CertificateManagerApp.service.CertificateService;
import pl.certificatemanager.CertificateManagerApp.service.SchedulerEmailService;

import java.time.LocalDateTime;
import java.time.ZoneId;

@RestController
@RequestMapping("/api/schedule")
@RequiredArgsConstructor
public class SchedulerController {
    private final SchedulerEmailService schedulerEmailService;
    private final CertificateService certificateService;

    @PostMapping("/email/certificate/{id}")
    public ResponseEntity<EmailResponse> scheduleEmail(@PathVariable("id") Long id, @RequestBody UserEmailRequest userEmailRequest) {
        Certificate certificate = certificateService.getCertificateById(id);

        Invoice invoice = certificate.getInvoice();
        Customer customer = invoice.getCustomer();

        LocalDateTime dateTime = LocalDateTime.now().atZone(ZoneId.of("CET")).toLocalDateTime().plusDays(3);

        EmailRequest emailRequest = new EmailRequest();
        emailRequest.setEmail(userEmailRequest.getEmail());
        emailRequest.setSubject("Certificate " + certificate.getSerialNumber() + " is going to expire at " + certificate.getValidTo() + ".");
        emailRequest.setBody("Certificate with serial number " + certificate.getSerialNumber() + " associated with email " + customer.getEmail() + " and invoice " + invoice.getInvoiceNumber() + " is going to expire at " + certificate.getValidTo() + ".");
        emailRequest.setDateTime(dateTime);
        emailRequest.setTimeZone(ZoneId.of("CET"));

        return ResponseEntity.ok().body(schedulerEmailService.scheduleEmail(emailRequest));
    }
}
