package pl.certificatemanager.CertificateManagerApp.repository;

import org.springframework.data.jpa.repository.JpaRepository;
import pl.certificatemanager.CertificateManagerApp.model.Certificate;

public interface CertificateRepo extends JpaRepository<Certificate, Long> {
    Certificate findCertificateById(Long id);
}
