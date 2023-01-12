package pl.certificatemanager.CertificateManagerApp.repository;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;
import pl.certificatemanager.CertificateManagerApp.model.Authority;

@Repository
public interface AuthorityRepo extends JpaRepository<Authority, Long> {
    Authority findByAuthority(String authority);
}
