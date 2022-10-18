package pl.certificatemanager.CertificateManagerApp;

import org.springframework.boot.CommandLineRunner;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.context.annotation.Bean;
import pl.certificatemanager.CertificateManagerApp.model.Certificate;
import pl.certificatemanager.CertificateManagerApp.model.Person;
import pl.certificatemanager.CertificateManagerApp.model.Authority;
import pl.certificatemanager.CertificateManagerApp.model.User;
import pl.certificatemanager.CertificateManagerApp.service.CertificateService;
import pl.certificatemanager.CertificateManagerApp.service.PersonService;
import pl.certificatemanager.CertificateManagerApp.service.AuthorityService;
import pl.certificatemanager.CertificateManagerApp.service.UserService;

import java.util.ArrayList;

@SpringBootApplication
public class CertificateManagerAppApplication {

	public static void main(String[] args) {
		SpringApplication.run(CertificateManagerAppApplication.class, args);
	}

	@Bean
	CommandLineRunner run(UserService userService, AuthorityService authorityService, PersonService personService, CertificateService certificateService) {
		return args -> {
//			Creating roles
//			authorityService.saveAuthority(new Authority(null, "ROLE_USER"));
//			authorityService.saveAuthority(new Authority(null, "ROLE_ADMIN"));

//			Creating users
//			userService.saveUser(new User(null, "user", "1234", new ArrayList<>()));
//			userService.saveUser(new User(null, "admin", "1234", new ArrayList<>()));

//			Adding roles to users
//			userService.addAuthorityToUser("user", "ROLE_USER");
//			userService.addAuthorityToUser("admin", "ROLE_USER");
//			userService.addAuthorityToUser("admin", "ROLE_ADMIN");

//			Creating people
//			personService.savePerson(new Person(null, "12345678901", "Katarzyna", "Katarzyńska", "123123123", "pies@gmail.com", "Katowicka 14", "Katowice", "12-345", false, null, null));
//			personService.savePerson(new Person(null, "09876543210", "Maciej", "Zurek", "890890890", "kot@gmail.com", "Tyszańska 4", "Tychy", "43-100", false, null, null));

//			Creating certificates
//			certificateService.saveCertificate(new Certificate(null, "CN=CA", "CN=Leaf", "17457422577940475105", "SHA256withRSA", "[B@20fa23c1", null, null));
//			certificateService.saveCertificate(new Certificate(null, "CN=CA", "CN=CA", "78957422577940475999", "SHA256withRSA", "[B@20fa23c9", null, null));
		};
	}

}
