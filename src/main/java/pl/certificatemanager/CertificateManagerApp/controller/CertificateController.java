package pl.certificatemanager.CertificateManagerApp.controller;

import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.servlet.support.ServletUriComponentsBuilder;
import pl.certificatemanager.CertificateManagerApp.model.Certificate;
import pl.certificatemanager.CertificateManagerApp.payload.StatusRequest;
import pl.certificatemanager.CertificateManagerApp.service.CertificateService;

import java.net.URI;
import java.util.List;

@RestController
@RequestMapping("/api/certificate")
@RequiredArgsConstructor
public class CertificateController {
    private final CertificateService certificateService;

    @GetMapping("/find/all")
    public ResponseEntity<List<Certificate>> getCertificates() {
        return ResponseEntity.ok().body(certificateService.getCertificates());
    }

    @GetMapping("/find/id/{id}")
    public ResponseEntity<Certificate> getCertificateById(@PathVariable("id") Long id) {
        return ResponseEntity.ok().body(certificateService.getCertificateById(id));
    }

    @PostMapping("/save")
    public ResponseEntity<Certificate> saveCertificate(@RequestBody Certificate certificate) {
        URI uri = URI.create(ServletUriComponentsBuilder.fromCurrentContextPath().path("/api/certificate/save").toUriString());
        return ResponseEntity.created(uri).body(certificateService.saveCertificate(certificate));
    }

    @DeleteMapping("/delete/{id}")
    public ResponseEntity<Certificate> deleteCertificate(@PathVariable("id") Long id) {
        return ResponseEntity.ok().body(certificateService.deleteCertificate(id));
    }

    @GetMapping("/find/all/serial/and/valid")
    public ResponseEntity<List<Object[]>> getAllSerialNumberAndValidTo() {
        return ResponseEntity.ok().body(certificateService.getAllSerialNumberAndValidTo());
    }

    @GetMapping("/find/all/serial/and/valid/invoice/id/{id}")
    public ResponseEntity<List<Object[]>> getAllSerialNumberAndValidToByInvoiceId(@PathVariable("id") Long id) {
        return ResponseEntity.ok().body(certificateService.getAllSerialNumberAndValidToByInvoiceId(id));
    }

    @PutMapping("/update/{id}/status")
    public ResponseEntity<Certificate> updateCertificateStatus(@PathVariable("id") Long id, @RequestBody StatusRequest request) {
        return ResponseEntity.ok().body(certificateService.updateCertificateStatus(id, request));
    }
}
