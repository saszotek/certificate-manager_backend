package pl.certificatemanager.CertificateManagerApp.service;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.quartz.*;
import org.springframework.stereotype.Service;
import pl.certificatemanager.CertificateManagerApp.message.ResponseMessage;
import pl.certificatemanager.CertificateManagerApp.model.Certificate;
import pl.certificatemanager.CertificateManagerApp.model.Customer;
import pl.certificatemanager.CertificateManagerApp.model.Invoice;
import pl.certificatemanager.CertificateManagerApp.model.User;
import pl.certificatemanager.CertificateManagerApp.payload.SchedulerStatusRequest;
import pl.certificatemanager.CertificateManagerApp.payload.StatusRequest;
import pl.certificatemanager.CertificateManagerApp.payload.ValidToRequest;
import pl.certificatemanager.CertificateManagerApp.repository.CertificateRepo;
import pl.certificatemanager.CertificateManagerApp.repository.CustomerRepo;
import pl.certificatemanager.CertificateManagerApp.util.MailUtil;

import javax.transaction.Transactional;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.time.LocalDateTime;
import java.time.ZoneId;
import java.time.format.DateTimeFormatter;
import java.util.Date;
import java.util.List;

@Service
@RequiredArgsConstructor
@Transactional
@Slf4j
public class CertificateService {
    private final CertificateRepo certificateRepo;
    private final CustomerRepo customerRepo;
    private final SchedulerStatusService schedulerStatusService;
    private final SchedulerEmailService schedulerEmailService;
    private final UserService userService;
    private final Scheduler scheduler;
    private final MailUtil mailUtil;

    public List<Certificate> getCertificates() {
        log.info("Fetching all certificates");
        return certificateRepo.findAll();
    }

    public Certificate getCertificateById(Long id) {
        log.info("Fetching certificate with id {}", id);
        return certificateRepo.findCertificateById(id);
    }

    public Certificate saveCertificate(Certificate certificate) {
        log.info("Saving a new certificate {} to the database", certificate.getSerialNumber());
        return certificateRepo.save(certificate);
    }

    public Certificate deleteCertificate(Long id) {
        Certificate certificate = getCertificateById(id);
        log.info("Deleting certificate {}", certificate);
        certificateRepo.delete(certificate);
        return certificate;
    }

    public List<Object[]> getAllSerialNumberAndValidTo() {
        log.info("Fetching all serial numbers and valid to");
        return certificateRepo.findAllSerialNumberAndValidTo();
    }

    public List<Object[]> getAllSerialNumberAndValidToByInvoiceId(Long id) {
        log.info("Fetching all serial numbers and valid to by invoice id {}", id);
        return certificateRepo.findAllSerialNumberAndValidToByInvoiceId(id);
    }

    public Certificate updateCertificateStatus(Long id, StatusRequest request) {
        log.info("Updating certificate {} with status {}", id, request.getStatus());
        Certificate certificate = certificateRepo.findCertificateById(id);
        certificate.setStatus(request.getStatus());
        Customer customer = customerRepo.findCustomerBySerialNumber(certificate.getSerialNumber());
        mailUtil.sendMail(customer.getEmail(), "Updated status of certificate " + certificate.getSerialNumber(), "Your certificate of serial number " + certificate.getSerialNumber() + " has been updated with a new status: " + request.getStatus() + ".");
        return certificate;
    }

    public ResponseMessage updateCertificateExpiration(Long id, ValidToRequest validToRequest) {
        ResponseMessage responseMessage = new ResponseMessage();

        try {
            Certificate certificate = certificateRepo.findCertificateById(id);

            SimpleDateFormat sdf = new SimpleDateFormat("yyyy-MM-dd'T'HH:mm:ss");
            Date date = sdf.parse(validToRequest.getValidTo());

            certificate.setValidTo(date);

            try {
                JobDetail jobDetailStatus = scheduler.getJobDetail(new JobKey(certificate.getSerialNumber(), "status-jobs"));
                scheduler.deleteJob(jobDetailStatus.getKey());

                log.info("Previous JobDetail regarding status was deleted successfully.");

                JobDetail jobDetailBeforeDays60 = scheduler.getJobDetail(new JobKey(certificate.getSerialNumber() + "-60", "email-jobs"));
                if (jobDetailBeforeDays60 != null) {
                    scheduler.deleteJob(jobDetailBeforeDays60.getKey());
                }

                JobDetail jobDetailBeforeDays30 = scheduler.getJobDetail(new JobKey(certificate.getSerialNumber() + "-30", "email-jobs"));
                if (jobDetailBeforeDays30 != null) {
                    scheduler.deleteJob(jobDetailBeforeDays30.getKey());
                }

                JobDetail jobDetailBeforeDays14 = scheduler.getJobDetail(new JobKey(certificate.getSerialNumber() + "-14", "email-jobs"));
                if (jobDetailBeforeDays14 != null) {
                    scheduler.deleteJob(jobDetailBeforeDays14.getKey());
                }

                JobDetail jobDetailBeforeDays7 = scheduler.getJobDetail(new JobKey(certificate.getSerialNumber() + "-7", "email-jobs"));
                if (jobDetailBeforeDays7 != null) {
                    scheduler.deleteJob(jobDetailBeforeDays7.getKey());
                }

                log.info("Previous JobDetail regarding scheduled emails was deleted successfully.");
            } catch (SchedulerException e) {
                log.error("Could not delete previous JobDetail regarding status or scheduled emails. Error: ", e);
            }

            Invoice invoice = certificate.getInvoice();
            Customer customer = invoice.getCustomer();

            LocalDateTime localDateTime = LocalDateTime.parse(validToRequest.getValidTo(), DateTimeFormatter.ISO_DATE_TIME);

            SchedulerStatusRequest schedulerStatusRequest = new SchedulerStatusRequest();
            schedulerStatusRequest.setSerialNumber(certificate.getSerialNumber());
            schedulerStatusRequest.setStatus("Expired");
            schedulerStatusRequest.setDateTime(localDateTime);
            schedulerStatusRequest.setTimeZone(ZoneId.of("CET"));

            schedulerStatusService.setStatus(schedulerStatusRequest);

            List<User> users = userService.getUsers();

            users.forEach(user -> {
                schedulerEmailService.setupEmailSchedule(user.getUsername(), customer.getEmail(), certificate.getSerialNumber(), invoice.getInvoiceNumber(), certificate.getValidTo(), 60);
                schedulerEmailService.setupEmailSchedule(user.getUsername(), customer.getEmail(), certificate.getSerialNumber(), invoice.getInvoiceNumber(), certificate.getValidTo(), 30);
                schedulerEmailService.setupEmailSchedule(user.getUsername(), customer.getEmail(), certificate.getSerialNumber(), invoice.getInvoiceNumber(), certificate.getValidTo(), 14);
                schedulerEmailService.setupEmailSchedule(user.getUsername(), customer.getEmail(), certificate.getSerialNumber(), invoice.getInvoiceNumber(), certificate.getValidTo(), 7);
            });

            log.info("Successfully extended date of certificate {} to {}", id, date);
            responseMessage.setMessage("Successfully extended date of certificate.");
            mailUtil.sendMail(customer.getEmail(), "The validity of the certificate " + certificate.getSerialNumber() + " has been extended", "Your certificate of serial number " + certificate.getSerialNumber() + " has been extended and is now valid until " + validToRequest.getValidTo() + ".");
            return responseMessage;
        } catch (ParseException e) {
            log.error("Could not extend date of certificate. Error: ", e);
            responseMessage.setMessage("Could not extend date of certificate.");
            return responseMessage;
        }
    }
}
