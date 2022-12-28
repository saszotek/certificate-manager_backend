package pl.certificatemanager.CertificateManagerApp.repository;

import org.springframework.data.jpa.repository.JpaRepository;
import pl.certificatemanager.CertificateManagerApp.model.Invoice;

public interface InvoiceRepo extends JpaRepository<Invoice, Long> {
    Invoice findInvoiceById(Long id);
    Boolean existsByInvoiceNumber(String invoiceNumber);
}
