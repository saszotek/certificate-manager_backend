package pl.certificatemanager.CertificateManagerApp.repository;

import org.springframework.data.jpa.repository.JpaRepository;
import pl.certificatemanager.CertificateManagerApp.model.Certificate;
import pl.certificatemanager.CertificateManagerApp.model.Invoice;

import java.util.List;

public interface CertificateRepo extends JpaRepository<Certificate, Long> {
    Certificate findCertificateById(Long id);
    Boolean existsBySerialNumber(String serialNumber);
    List<Certificate> findCertificatesByInvoice(Invoice invoice);
}
