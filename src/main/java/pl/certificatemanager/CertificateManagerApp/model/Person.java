package pl.certificatemanager.CertificateManagerApp.model;

import com.fasterxml.jackson.annotation.JsonIgnore;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import javax.persistence.*;
import java.util.ArrayList;
import java.util.List;

@Entity
@Data
@NoArgsConstructor
@AllArgsConstructor
public class Person {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;
    private String pesel;
    @Column(name = "first_name")
    private String firstName;
    @Column(name = "last_name")
    private String lastName;
    @Column(name = "phone_number")
    private String phoneNumber;
    private String email;
    @Column(name = "residential_address")
    private String residentialAddress;
    private String city;
    @Column(name = "postal_code")
    private String postalCode;
    @Column(name = "certificate_request")
    private String certificateRequest = "false";
    @Column(name = "certificate_status")
    private String certificateStatus;
    @OneToOne(cascade = CascadeType.ALL)
    @JoinColumn(name = "certificate_id")
    private Certificate certificate;
    @OneToOne(cascade = CascadeType.ALL)
    @JoinColumn(name = "user_id")
    private User user;
    @OneToMany(mappedBy = "person",
            cascade = CascadeType.REMOVE
    )
    private List<Payment> payments = new ArrayList<>();

    public void addPayment(Payment payment) {
        payments.add(payment);
    }

    public void deletePayment(Payment payment) {
        payments.remove(payment);
    }
}
