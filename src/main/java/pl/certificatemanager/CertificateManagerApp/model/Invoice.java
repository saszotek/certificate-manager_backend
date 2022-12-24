package pl.certificatemanager.CertificateManagerApp.model;

import com.fasterxml.jackson.annotation.JsonIgnore;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import javax.persistence.*;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;

@Entity
@Data
@NoArgsConstructor
@AllArgsConstructor
public class Invoice {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;
    @Column(name = "invoice_number")
    private String invoiceNumber;
    @Column(name = "date_of_agreement")
    @Temporal(TemporalType.TIMESTAMP)
    private Date dateOfAgreement;
    private String status;
    @ManyToOne
    @JsonIgnore
    @JoinColumn(name = "customer_id")
    private Customer customer;
    @OneToMany(mappedBy = "invoice", cascade = CascadeType.REMOVE)
    private List<Certificate> certificates = new ArrayList<>();

    public void addCertificate(Certificate certificate) {
        certificates.add(certificate);
    }

    public void deleteCertificate(Certificate certificate) {
        certificates.remove(certificate);
    }
}
