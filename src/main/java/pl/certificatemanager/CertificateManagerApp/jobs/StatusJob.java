package pl.certificatemanager.CertificateManagerApp.jobs;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.quartz.JobDataMap;
import org.quartz.JobExecutionContext;
import org.springframework.scheduling.quartz.QuartzJobBean;
import org.springframework.stereotype.Component;
import pl.certificatemanager.CertificateManagerApp.model.Certificate;
import pl.certificatemanager.CertificateManagerApp.model.Customer;
import pl.certificatemanager.CertificateManagerApp.repository.CertificateRepo;
import pl.certificatemanager.CertificateManagerApp.repository.CustomerRepo;
import pl.certificatemanager.CertificateManagerApp.util.MailUtil;

@Component
@RequiredArgsConstructor
@Slf4j
public class StatusJob extends QuartzJobBean {
    private final CertificateRepo certificateRepo;
    private final CustomerRepo customerRepo;
    private final MailUtil mailUtil;

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
        Customer customer = customerRepo.findCustomerBySerialNumber(certificate.getSerialNumber());
        mailUtil.sendMail(customer.getEmail(), "The certificate " + certificate.getSerialNumber() + " is now expired", "Your certificate of serial number " + certificate.getSerialNumber() + " has expired and is no longer valid.");
        certificateRepo.save(certificate);
    }
}
