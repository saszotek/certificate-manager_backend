package pl.certificatemanager.CertificateManagerApp.model;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import javax.persistence.*;
import java.util.Date;

@Entity
@Data
@NoArgsConstructor
@AllArgsConstructor
public class Certificate {
    @Id
    @GeneratedValue(strategy = GenerationType.AUTO)
    private Long id;

    private String issuer;

    private String subject;

    @Column(name = "serial_number")
    private String serialNumber;

    @Column(name = "signature_algorithm")
    private String signatureAlgorithm;

    @Column(name = "public_key")
    private String publicKey;

    @Column(name = "valid_from")
    @Temporal(TemporalType.TIMESTAMP)
    private Date validFrom;

    @Column(name = "valid_to")
    @Temporal(TemporalType.TIMESTAMP)
    private Date validTo;
}
