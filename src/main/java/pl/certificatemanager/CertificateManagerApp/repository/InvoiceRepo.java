package pl.certificatemanager.CertificateManagerApp.repository;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;
import pl.certificatemanager.CertificateManagerApp.model.Customer;
import pl.certificatemanager.CertificateManagerApp.model.Invoice;

import java.util.List;

@Repository
public interface InvoiceRepo extends JpaRepository<Invoice, Long> {
    Invoice findInvoiceById(Long id);
    Boolean existsByInvoiceNumber(String invoiceNumber);
    Invoice findByInvoiceNumber(String invoice);
    List<Invoice> findInvoicesByCustomer(Customer customer);
}
