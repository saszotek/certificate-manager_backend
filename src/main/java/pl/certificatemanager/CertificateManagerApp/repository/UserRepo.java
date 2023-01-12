package pl.certificatemanager.CertificateManagerApp.repository;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;
import pl.certificatemanager.CertificateManagerApp.model.User;

@Repository
public interface UserRepo extends JpaRepository<User, Long> {
    User findUserById(Long id);
    User findByUsername(String username);
    Boolean existsByUsername(String username);
}
