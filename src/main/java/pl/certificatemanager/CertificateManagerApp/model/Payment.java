package pl.certificatemanager.CertificateManagerApp.model;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import javax.persistence.*;

@Entity
@Data
@NoArgsConstructor
@AllArgsConstructor
public class Payment {
    @Id
    @GeneratedValue(strategy = GenerationType.AUTO)
    private Long id;
    private String nip;
    @Column(name = "account_number")
    private String accountNumber;
    private String swift;
    @ManyToOne
    @JoinColumn(name = "person_id")
    private Person person;
}
