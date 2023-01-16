package pl.certificatemanager.CertificateManagerApp.payload;

import com.fasterxml.jackson.annotation.JsonFormat;
import lombok.Data;

@Data
public class ValidToRequest {
    @JsonFormat(pattern="yyyy-MM-dd'T'HH:mm:ss")
    private String validTo;
    private String serialNumber;
}
