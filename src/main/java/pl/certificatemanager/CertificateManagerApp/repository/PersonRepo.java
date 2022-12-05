package pl.certificatemanager.CertificateManagerApp.repository;

import org.springframework.data.jpa.repository.JpaRepository;
import pl.certificatemanager.CertificateManagerApp.model.Person;
import pl.certificatemanager.CertificateManagerApp.model.User;

public interface PersonRepo extends JpaRepository<Person, Long> {
    Person findPersonById(Long id);
    Person findPersonByUser(User user);
}
