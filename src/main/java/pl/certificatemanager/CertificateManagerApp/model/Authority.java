package pl.certificatemanager.CertificateManagerApp.model;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
import org.springframework.security.core.GrantedAuthority;

import javax.persistence.*;

@Entity
@Data
@NoArgsConstructor
@AllArgsConstructor
public class Authority implements GrantedAuthority {
    @Id
    @GeneratedValue(strategy = GenerationType.AUTO)
    private Long id;
    private String authority;
    @ManyToOne
    @JoinColumn(name = "user_id")
    private User user;

    public Authority(String authority) {
        this.authority = authority;
    }
}
