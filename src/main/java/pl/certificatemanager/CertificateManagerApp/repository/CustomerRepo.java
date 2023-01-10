package pl.certificatemanager.CertificateManagerApp.repository;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import pl.certificatemanager.CertificateManagerApp.model.Customer;

public interface CustomerRepo extends JpaRepository<Customer, Long> {
    Customer findCustomerById(Long id);
    Boolean existsByEmail(String email);
    Customer findByEmail(String email);
    Page<Customer> findByEmailContaining(String email, Pageable pageable);
}
