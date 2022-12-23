package pl.certificatemanager.CertificateManagerApp.controller;

import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.servlet.support.ServletUriComponentsBuilder;
import pl.certificatemanager.CertificateManagerApp.model.Invoice;
import pl.certificatemanager.CertificateManagerApp.service.InvoiceService;

import java.net.URI;
import java.util.List;

@RestController
@RequestMapping("/api/invoice")
@RequiredArgsConstructor
public class InvoiceController {
    private final InvoiceService invoiceService;

    @GetMapping("/find/all")
    public ResponseEntity<List<Invoice>> getInvoices() {
        return ResponseEntity.ok().body(invoiceService.getInvoices());
    }

    @GetMapping("/find/id/{id}")
    public ResponseEntity<Invoice> getInvoiceById(@PathVariable("id") Long id) {
        return ResponseEntity.ok().body(invoiceService.getInvoiceById(id));
    }

    @PostMapping("/save")
    public ResponseEntity<Invoice> saveInvoice(@RequestBody Invoice invoice) {
        URI uri = URI.create(ServletUriComponentsBuilder.fromCurrentContextPath().path("/api/invoice/save").toUriString());
        return ResponseEntity.created(uri).body(invoiceService.saveInvoice(invoice));
    }

    @DeleteMapping("/delete/{id}")
    public ResponseEntity<Invoice> deleteInvoice(@PathVariable("id") Long id) {
        return ResponseEntity.ok().body(invoiceService.deleteInvoice(id));
    }

    @PostMapping("/save/{invoiceId}/certificate/{certificateId}")
    public ResponseEntity<Invoice> saveCertificateToInvoice(@PathVariable("invoiceId") Long invoiceId, @PathVariable("certificateId") Long certificateId) {
        Invoice invoice = invoiceService.saveCertificateToInvoice(invoiceId, certificateId);
        return new ResponseEntity<>(invoice, HttpStatus.OK);
    }

    @DeleteMapping("/delete/{invoiceId}/certificate/{certificateId}")
    public ResponseEntity<Invoice> deleteCertificateFromInvoice(@PathVariable("invoiceId") Long invoiceId, @PathVariable("certificateId") Long certificateId) {
        Invoice invoice = invoiceService.deleteCertificateFromInvoice(invoiceId, certificateId);
        return new ResponseEntity<>(invoice, HttpStatus.OK);
    }
}
