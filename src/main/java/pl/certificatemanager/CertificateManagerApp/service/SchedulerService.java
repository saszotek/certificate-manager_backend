package pl.certificatemanager.CertificateManagerApp.service;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.quartz.*;
import org.springframework.stereotype.Service;
import pl.certificatemanager.CertificateManagerApp.jobs.EmailJob;
import pl.certificatemanager.CertificateManagerApp.payload.EmailRequest;
import pl.certificatemanager.CertificateManagerApp.payload.EmailResponse;

import java.sql.Date;
import java.time.LocalDateTime;
import java.time.ZoneId;
import java.time.ZonedDateTime;
import java.util.UUID;

@Slf4j
@RequiredArgsConstructor
@Service
public class SchedulerService {
    private final Scheduler scheduler;

    public EmailResponse scheduleEmail(EmailRequest emailRequest) {
        try {
            ZonedDateTime dateTime = ZonedDateTime.of(emailRequest.getDateTime(), emailRequest.getTimeZone());

            if (dateTime.isBefore(ZonedDateTime.now())) {
                EmailResponse emailResponse = new EmailResponse(false, "dateTime must be after current time.");
                log.error("dateTime must be after current time.");
                return emailResponse;
            }

            JobDetail jobDetail = buildJobDetail(emailRequest);
            Trigger trigger = buildTrigger(jobDetail, dateTime);

            scheduler.scheduleJob(jobDetail, trigger);

            EmailResponse emailResponse = new EmailResponse(true, jobDetail.getKey().getName(), jobDetail.getKey().getGroup(), "Email scheduled successfully.");

            return emailResponse;
        } catch (SchedulerException e) {
            log.error("Error while  scheduling email. Error: ", e);
            EmailResponse emailResponse = new EmailResponse(false, "Error while scheduling email.");
            return emailResponse;
        }
    }

    public void setupEmailSchedule(String emailRecipient, String emailCustomer, String serialNumber, String invoiceNumber, java.util.Date validTo, Integer sendBeforeDays) {
        LocalDateTime dateTime = validTo.toInstant().atZone(ZoneId.of("CET")).toLocalDateTime().minusDays(sendBeforeDays);

        log.info(String.valueOf(dateTime));

        EmailRequest emailRequest = new EmailRequest();
        emailRequest.setEmail(emailRecipient);
        emailRequest.setSubject("Certificate " + serialNumber + " is going to expire in " + sendBeforeDays + " days.");
        emailRequest.setBody("Certificate with serial number " + serialNumber + " associated with email " + emailCustomer + " and invoice " + invoiceNumber + " is going to expire in " + sendBeforeDays + " days.");
        emailRequest.setDateTime(dateTime);
        emailRequest.setTimeZone(ZoneId.of("CET"));

        scheduleEmail(emailRequest);
    }

    private JobDetail buildJobDetail(EmailRequest scheduleEmailRequest) {
        JobDataMap jobDataMap = new JobDataMap();

        jobDataMap.put("email", scheduleEmailRequest.getEmail());
        jobDataMap.put("subject", scheduleEmailRequest.getSubject());
        jobDataMap.put("body", scheduleEmailRequest.getBody());

        return JobBuilder.newJob(EmailJob.class)
                .withIdentity(UUID.randomUUID().toString(), "email-jobs")
                .withDescription("Send Email Job")
                .usingJobData(jobDataMap)
                .storeDurably()
                .build();
    }

    private Trigger buildTrigger(JobDetail jobDetail, ZonedDateTime startAt) {
        return TriggerBuilder.newTrigger()
                .forJob(jobDetail)
                .withIdentity(jobDetail.getKey().getName(), "email-triggers")
                .withDescription("Send Email Trigger")
                .startAt(Date.from(startAt.toInstant()))
                .withSchedule(SimpleScheduleBuilder.simpleSchedule().withMisfireHandlingInstructionFireNow())
                .build();
    }
}
