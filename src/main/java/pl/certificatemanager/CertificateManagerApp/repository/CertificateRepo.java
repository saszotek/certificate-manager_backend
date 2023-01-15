package pl.certificatemanager.CertificateManagerApp.repository;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;
import pl.certificatemanager.CertificateManagerApp.model.Certificate;
import pl.certificatemanager.CertificateManagerApp.model.Invoice;

import java.util.List;

@Repository
public interface CertificateRepo extends JpaRepository<Certificate, Long> {
    Certificate findCertificateById(Long id);
    Certificate findCertificateBySerialNumber(String serialNumber);
    Boolean existsBySerialNumber(String serialNumber);
    List<Certificate> findCertificatesByInvoice(Invoice invoice);
    @Query("SELECT c.serialNumber, c.validTo  FROM Certificate c")
    List<Object[]> findAllSerialNumberAndValidTo();
    @Query("SELECT c.serialNumber, c.validTo FROM Certificate c WHERE c.invoice.id = :id")
    List<Object[]> findAllSerialNumberAndValidToByInvoiceId(@Param("id") Long id);
}
