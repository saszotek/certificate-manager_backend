package pl.certificatemanager.CertificateManagerApp.message;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;

@Data
@RequiredArgsConstructor
@AllArgsConstructor
@Component
public class ResponseMessage {
    private String message;
}
