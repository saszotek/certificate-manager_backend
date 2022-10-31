package pl.certificatemanager.CertificateManagerApp.controller;

import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.servlet.support.ServletUriComponentsBuilder;
import pl.certificatemanager.CertificateManagerApp.model.Authority;
import pl.certificatemanager.CertificateManagerApp.service.AuthorityService;

import java.net.URI;

@RestController
@RequestMapping("/api/authority")
@RequiredArgsConstructor
public class AuthorityController {
    private final AuthorityService authorityService;

    @PostMapping("/save")
    public ResponseEntity<Authority> saveUser(@RequestBody Authority authority) {
        URI uri = URI.create(ServletUriComponentsBuilder.fromCurrentContextPath().path("/api/authority/save").toUriString());
        return ResponseEntity.created(uri).body(authorityService.saveAuthority(authority));
    }
}
