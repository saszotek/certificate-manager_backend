package pl.certificatemanager.CertificateManagerApp.payload;

import lombok.Data;

@Data
public class AuthCredentialsRequest {
    private String username;
    private String password;
}
