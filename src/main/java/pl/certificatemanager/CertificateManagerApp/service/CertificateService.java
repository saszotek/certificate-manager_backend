package pl.certificatemanager.CertificateManagerApp.service;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import pl.certificatemanager.CertificateManagerApp.model.Certificate;
import pl.certificatemanager.CertificateManagerApp.repository.CertificateRepo;

import javax.transaction.Transactional;
import java.util.List;

@Service
@RequiredArgsConstructor
@Transactional
@Slf4j
public class CertificateService {
    private final CertificateRepo certificateRepo;

    public List<Certificate> getCertificates() {
        log.info("Fetching all certificates");
        return certificateRepo.findAll();
    }

    public Certificate getCertificateById(Long id) {
        log.info("Fetching certificate with id {}", id);
        return certificateRepo.findCertificateById(id);
    }

    public Certificate saveCertificate(Certificate certificate) {
        log.info("Saving a new certificate {} to the database", certificate.getSerialNumber());
        return certificateRepo.save(certificate);
    }

    public Certificate deleteCertificate(Long id) {
        Certificate certificate = getCertificateById(id);
        log.info("Deleting certificate {}", certificate);
        certificateRepo.delete(certificate);
        return certificate;
    }

    public List<Object[]> getAllSerialNumberAndValidTo() {
        log.info("Fetching all serial numbers and valid to");
        return certificateRepo.findAllSerialNumberAndValidTo();
    }

    public List<Object[]> getAllSerialNumberAndValidToByInvoiceId(Long id) {
        log.info("Fetching all serial numbers and valid to by invoice id {}", id);
        return certificateRepo.findAllSerialNumberAndValidToByInvoiceId(id);
    }
}
