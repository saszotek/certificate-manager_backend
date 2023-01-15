package pl.certificatemanager.CertificateManagerApp.jobs;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.quartz.JobDataMap;
import org.quartz.JobExecutionContext;
import org.springframework.scheduling.quartz.QuartzJobBean;
import org.springframework.stereotype.Component;
import pl.certificatemanager.CertificateManagerApp.model.Certificate;
import pl.certificatemanager.CertificateManagerApp.repository.CertificateRepo;

@Component
@RequiredArgsConstructor
@Slf4j
public class StatusJob extends QuartzJobBean {
    private final CertificateRepo certificateRepo;

    @Override
    protected void executeInternal(JobExecutionContext jobExecutionContext) {
        JobDataMap jobDataMap = jobExecutionContext.getMergedJobDataMap();

        String serialNumber = jobDataMap.getString("serialNumber");
        String status = jobDataMap.getString("status");

        setStatus(serialNumber, status);
    }

    private void setStatus(String serialNumber, String status) {
        Certificate certificate = certificateRepo.findCertificateBySerialNumber(serialNumber);
        certificate.setStatus(status);
        certificateRepo.save(certificate);
    }
}
