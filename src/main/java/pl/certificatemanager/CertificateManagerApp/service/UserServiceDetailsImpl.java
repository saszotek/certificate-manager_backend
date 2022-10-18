package pl.certificatemanager.CertificateManagerApp.service;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.stereotype.Service;
import pl.certificatemanager.CertificateManagerApp.model.User;
import pl.certificatemanager.CertificateManagerApp.util.CustomPasswordEncoder;

@Service
public class UserServiceDetailsImpl implements UserDetailsService {
    @Autowired
    private CustomPasswordEncoder customPasswordEncoder;

    @Override
    public UserDetails loadUserByUsername(String username) throws UsernameNotFoundException {
        User user = new User();
        user.setUsername(username);
        user.setPassword(customPasswordEncoder.getPasswordEncoder().encode("1234"));
        user.setId(1L);
        return user;
    }
}
