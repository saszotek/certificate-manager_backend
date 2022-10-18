package pl.certificatemanager.CertificateManagerApp.service;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import pl.certificatemanager.CertificateManagerApp.model.Authority;
import pl.certificatemanager.CertificateManagerApp.repository.AuthorityRepo;

import javax.transaction.Transactional;

@Service
@RequiredArgsConstructor
@Transactional
@Slf4j
public class AuthorityService {
    private final AuthorityRepo authorityRepo;

    public Authority saveAuthority(Authority authority) {
        log.info("Saving new authority {} to the database", authority.getAuthority());
        return authorityRepo.save(authority);
    }
}
