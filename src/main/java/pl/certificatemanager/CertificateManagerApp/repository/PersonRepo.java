package pl.certificatemanager.CertificateManagerApp.repository;

import org.springframework.data.jpa.repository.JpaRepository;
import pl.certificatemanager.CertificateManagerApp.model.Person;

public interface PersonRepo extends JpaRepository<Person, Long> {
    Person findPersonById(Long id);
}
