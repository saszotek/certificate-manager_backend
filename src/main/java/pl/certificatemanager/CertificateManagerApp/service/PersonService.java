package pl.certificatemanager.CertificateManagerApp.service;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import pl.certificatemanager.CertificateManagerApp.model.Person;
import pl.certificatemanager.CertificateManagerApp.repository.PersonRepo;

import javax.transaction.Transactional;
import java.util.List;

@Service
@RequiredArgsConstructor
@Transactional
@Slf4j
public class PersonService {
    private final PersonRepo personRepo;

    public List<Person> getPersons() {
        log.info("Fetching all people");
        return personRepo.findAll();
    }

    public Person getPersonById(Long id) {
        log.info("Fetching person with id {}", id);
        return personRepo.findPersonById(id);
    }

    public Person savePerson(Person person) {
        log.info("Saving new person {} to the database", person.getLastName());
        return personRepo.save(person);
    }

    public Person deletePerson(Long id) {
        Person person = getPersonById(id);
        log.info("Deleting person {}", person);
        personRepo.delete(person);
        return person;
    }
}
