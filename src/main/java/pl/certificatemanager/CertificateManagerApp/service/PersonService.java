package pl.certificatemanager.CertificateManagerApp.service;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import pl.certificatemanager.CertificateManagerApp.exception.PaymentAlreadyAssignedPersonException;
import pl.certificatemanager.CertificateManagerApp.model.Payment;
import pl.certificatemanager.CertificateManagerApp.model.Person;
import pl.certificatemanager.CertificateManagerApp.repository.PersonRepo;

import javax.transaction.Transactional;
import java.util.List;
import java.util.Objects;

@Service
@RequiredArgsConstructor
@Transactional
@Slf4j
public class PersonService {
    private final PersonRepo personRepo;
    private final PaymentService paymentService;

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

    public Person addPaymentToPerson(Long personId, Long paymentId) {
        Person person = getPersonById(personId);
        Payment payment = paymentService.getPaymentById(paymentId);
        if (Objects.nonNull(payment.getPerson())) {
            throw new PaymentAlreadyAssignedPersonException(paymentId, payment.getPerson().getId());
        }
        log.info("Payment info: {} is added to the person: {}", paymentId, personId);
        person.addPayment(payment);
        payment.setPerson(person);
        return person;
    }
}
