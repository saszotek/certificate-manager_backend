package pl.certificatemanager.CertificateManagerApp.repository;

import org.springframework.data.jpa.repository.JpaRepository;
import pl.certificatemanager.CertificateManagerApp.model.Customer;

public interface CustomerRepo extends JpaRepository<Customer, Long> {
    Customer findCustomerById(Long id);
}
