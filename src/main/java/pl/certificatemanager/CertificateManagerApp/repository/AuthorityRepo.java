package pl.certificatemanager.CertificateManagerApp.repository;

import org.springframework.data.jpa.repository.JpaRepository;
import pl.certificatemanager.CertificateManagerApp.model.Authority;

public interface AuthorityRepo extends JpaRepository<Authority, Long> {
    Authority findByAuthority(String authority);
}
