package pl.certificatemanager.CertificateManagerApp.service;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import pl.certificatemanager.CertificateManagerApp.model.User;
import pl.certificatemanager.CertificateManagerApp.repository.UserRepo;

import javax.transaction.Transactional;
import java.util.List;

@Service
@RequiredArgsConstructor
@Transactional
@Slf4j
public class UserService {
    private final UserRepo userRepo;

    public List<User> getUsers() {
        log.info("Fetching all users");
        return userRepo.findAll();
    }

    public User getUserById(Long id) {
        log.info("Fetching user with id {}", id);
        return userRepo.findUserById(id);
    }

    public User getUserByUsername(String username) {
        log.info("Fetching user with username {}", username);
        return userRepo.findByUsername(username);
    }

    public User saveUser(User user) {
        log.info("Saving new user {} to the database", user.getUsername());
        return userRepo.save(user);
    }

    public User deleteUser(Long id) {
        User user = getUserById(id);
        log.info("Deleting user {}", user);
        userRepo.delete(user);
        return user;
    }
}
