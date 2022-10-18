package pl.certificatemanager.CertificateManagerApp.controller;

import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.servlet.support.ServletUriComponentsBuilder;
import pl.certificatemanager.CertificateManagerApp.model.Person;
import pl.certificatemanager.CertificateManagerApp.service.PersonService;

import java.net.URI;
import java.util.List;

@RestController
@RequestMapping("/person")
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
        URI uri = URI.create(ServletUriComponentsBuilder.fromCurrentContextPath().path("/person/save").toUriString());
        return ResponseEntity.created(uri).body(personService.savePerson(person));
    }

    @DeleteMapping("delete/{id}")
    public ResponseEntity<Person> deletePerson(@PathVariable("id") Long id) {
        return ResponseEntity.ok().body(personService.deletePerson(id));
    }
}
