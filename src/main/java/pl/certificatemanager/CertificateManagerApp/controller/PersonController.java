package pl.certificatemanager.CertificateManagerApp.controller;

import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.servlet.support.ServletUriComponentsBuilder;
import pl.certificatemanager.CertificateManagerApp.model.Person;
import pl.certificatemanager.CertificateManagerApp.service.PersonService;

import java.net.URI;
import java.util.List;

@RestController
@RequestMapping("/api/person")
@RequiredArgsConstructor
public class PersonController {
    private final PersonService personService;

    @GetMapping("/find/all")
    public ResponseEntity<List<Person>> getPersons() {
        return ResponseEntity.ok().body(personService.getPersons());
    }

    @GetMapping("/find/id/{id}")
    public ResponseEntity<Person> getPersonById(@PathVariable("id") Long id) {
        return ResponseEntity.ok().body(personService.getPersonById(id));
    }

    @PostMapping("/save")
    public ResponseEntity<Person> savePerson(@RequestBody Person person) {
        URI uri = URI.create(ServletUriComponentsBuilder.fromCurrentContextPath().path("/api/person/save").toUriString());
        return ResponseEntity.created(uri).body(personService.savePerson(person));
    }

    @DeleteMapping("/delete/{id}")
    public ResponseEntity<Person> deletePerson(@PathVariable("id") Long id) {
        return ResponseEntity.ok().body(personService.deletePerson(id));
    }

    @PostMapping("/save/{personId}/payment/{paymentId}")
    public ResponseEntity<Person> savePaymentToPerson(@PathVariable("personId") Long personId, @PathVariable("paymentId") Long paymentId) {
        Person person = personService.savePaymentToPerson(personId, paymentId);
        return new ResponseEntity<>(person, HttpStatus.OK);
    }

    @DeleteMapping("/delete/{personId}/payment/{paymentId}")
    public ResponseEntity<Person> deletePaymentFromPerson(@PathVariable("personId") Long personId, @PathVariable("paymentId") Long paymentId) {
        Person person = personService.deletePaymentFromPerson(personId, paymentId);
        return new ResponseEntity<>(person, HttpStatus.OK);
    }

    @PostMapping("/save/{personId}/certificate/{certificateId}")
    public ResponseEntity<Person> saveCertificateToPerson(@PathVariable("personId") Long personId, @PathVariable("certificateId") Long certificateId) {
        Person person = personService.saveCertificateToPerson(personId, certificateId);
        return new ResponseEntity<>(person, HttpStatus.OK);
    }

    @PostMapping("/save/{personId}/user/{userId}")
    public ResponseEntity<Person> saveUserToPerson(@PathVariable("personId") Long personId, @PathVariable("userId") Long userId) {
        Person person = personService.saveUserToPerson(personId, userId);
        return new ResponseEntity<>(person, HttpStatus.OK);
    }
}
