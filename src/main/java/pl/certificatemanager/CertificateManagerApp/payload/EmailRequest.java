package pl.certificatemanager.CertificateManagerApp.payload;

import lombok.Data;

import java.time.LocalDateTime;
import java.time.ZoneId;

@Data
public class EmailRequest {
    private String email;
    private String subject;
    private String body;
    private LocalDateTime dateTime;
    private ZoneId timeZone;
}
