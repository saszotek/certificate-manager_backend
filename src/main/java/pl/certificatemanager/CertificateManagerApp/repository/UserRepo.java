package pl.certificatemanager.CertificateManagerApp.repository;

import org.springframework.data.jpa.repository.JpaRepository;
import pl.certificatemanager.CertificateManagerApp.model.User;

public interface UserRepo extends JpaRepository<User, Long> {
    User findUserById(Long id);
    User findByUsername(String username);
}
