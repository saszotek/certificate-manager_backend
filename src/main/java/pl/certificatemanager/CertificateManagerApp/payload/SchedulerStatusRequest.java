package pl.certificatemanager.CertificateManagerApp.payload;

import lombok.Data;

import java.time.LocalDateTime;
import java.time.ZoneId;

@Data
public class SchedulerStatusRequest {
    private String serialNumber;
    private String status;
    private LocalDateTime dateTime;
    private ZoneId timeZone;
}
