package pl.certificatemanager.CertificateManagerApp.controller;

import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.servlet.support.ServletUriComponentsBuilder;
import pl.certificatemanager.CertificateManagerApp.model.Payment;
import pl.certificatemanager.CertificateManagerApp.service.PaymentService;

import java.net.URI;
import java.util.List;

@RestController
@RequestMapping("/api/payment")
@RequiredArgsConstructor
public class PaymentController {
    public final PaymentService paymentService;

    @GetMapping("/find/all")
    public ResponseEntity<List<Payment>> getPayments() {
        return ResponseEntity.ok().body(paymentService.getAllPayments());
    }

    @GetMapping("/find/id/{id}")
    public ResponseEntity<Payment> getPaymentById(@PathVariable("id") Long id) {
        return ResponseEntity.ok().body(paymentService.getPaymentById(id));
    }

    @PostMapping("/save")
    public ResponseEntity<Payment> savePayment(@RequestBody Payment payment) {
        URI uri = URI.create(ServletUriComponentsBuilder.fromCurrentContextPath().path("/api/payment/save").toUriString());
        return ResponseEntity.created(uri).body(paymentService.savePayment(payment));
    }

    @DeleteMapping("/delete/{id}")
    public ResponseEntity<Payment> deletePayment(@PathVariable("id") Long id) {
        return ResponseEntity.ok().body(paymentService.deletePayment(id));
    }

}
