package pl.certificatemanager.CertificateManagerApp.payload;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.RequiredArgsConstructor;

@AllArgsConstructor
@RequiredArgsConstructor
@Data
public class EmailResponse {
    private boolean success;
    private String jobId;
    private String jobGroup;
    private String message;

    public EmailResponse(Boolean success, String message) {
        this.success = success;
        this.message = message;
    }
}
