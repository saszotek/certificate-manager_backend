package pl.certificatemanager.CertificateManagerApp.repository;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;
import pl.certificatemanager.CertificateManagerApp.model.Customer;

@Repository
public interface CustomerRepo extends JpaRepository<Customer, Long> {
    Customer findCustomerById(Long id);
    Boolean existsByEmail(String email);
    Boolean existsByPhoneNumber(String phoneNumber);
    Customer findByEmail(String email);
    Page<Customer> findByEmailContaining(String email, Pageable pageable);
    @Query("SELECT c FROM Customer c INNER JOIN c.invoices i ON c.id = i.customer.id INNER JOIN i.certificates ce ON i.id = ce.invoice.id GROUP BY c ORDER BY ce.validTo ASC")
    Page<Customer> findCustomersByValidToAsc(Pageable pageable);
    @Query("SELECT c FROM Customer c INNER JOIN c.invoices i ON c.id = i.customer.id INNER JOIN i.certificates ce ON i.id = ce.invoice.id GROUP BY c ORDER BY ce.validTo DESC")
    Page<Customer> findCustomersByValidToDesc(Pageable pageable);
    @Query("SELECT c FROM Customer c INNER JOIN c.invoices i ON c.id = i.customer.id INNER JOIN i.certificates ce ON i.id = ce.invoice.id WHERE ce.serialNumber = :serialNumber")
    Customer findCustomerBySerialNumber(@Param("serialNumber") String serialNumber);
}
