package pl.certificatemanager.CertificateManagerApp.repository;

import org.springframework.data.jpa.repository.JpaRepository;
import pl.certificatemanager.CertificateManagerApp.model.Role;

public interface RoleRepo extends JpaRepository<Role, Long> {
    Role findByRoleName(String roleName);
}
