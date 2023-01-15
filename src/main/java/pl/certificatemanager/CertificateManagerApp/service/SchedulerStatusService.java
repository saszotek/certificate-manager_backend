package pl.certificatemanager.CertificateManagerApp.service;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.quartz.*;
import org.springframework.stereotype.Service;
import pl.certificatemanager.CertificateManagerApp.jobs.StatusJob;
import pl.certificatemanager.CertificateManagerApp.model.Certificate;
import pl.certificatemanager.CertificateManagerApp.payload.SchedulerStatusRequest;
import pl.certificatemanager.CertificateManagerApp.repository.CertificateRepo;

import java.sql.Date;
import java.time.ZonedDateTime;
import java.util.UUID;

@Slf4j
@RequiredArgsConstructor
@Service
public class SchedulerStatusService {
    private final Scheduler scheduler;
    private final CertificateRepo certificateRepo;

    public void setStatus(SchedulerStatusRequest schedulerStatusRequest) {
        try {
            ZonedDateTime dateTime = ZonedDateTime.of(schedulerStatusRequest.getDateTime(), schedulerStatusRequest.getTimeZone());

            if (dateTime.isBefore(ZonedDateTime.now())) {
                Certificate certificate = certificateRepo.findCertificateBySerialNumber(schedulerStatusRequest.getSerialNumber());
                certificate.setStatus("Expired");
                certificateRepo.save(certificate);
                return;
            }

            JobDetail jobDetail = buildJobDetail(schedulerStatusRequest);
            Trigger trigger = buildTrigger(jobDetail, dateTime);

            scheduler.scheduleJob(jobDetail, trigger);
        } catch (SchedulerException e) {
            log.error("Error when changing certificate status. Error: ", e);
        }
    }

    private JobDetail buildJobDetail(SchedulerStatusRequest schedulerStatusRequest) {
        JobDataMap jobDataMap = new JobDataMap();

        jobDataMap.put("serialNumber", schedulerStatusRequest.getSerialNumber());
        jobDataMap.put("status", schedulerStatusRequest.getStatus());

        return JobBuilder.newJob(StatusJob.class)
                .withIdentity(UUID.randomUUID().toString(), "status-jobs")
                .withDescription("Set Status Job")
                .usingJobData(jobDataMap)
                .storeDurably()
                .build();
    }

    private Trigger buildTrigger(JobDetail jobDetail, ZonedDateTime startAt) {
        return TriggerBuilder.newTrigger()
                .forJob(jobDetail)
                .withIdentity(jobDetail.getKey().getName(), "status-triggers")
                .withDescription("Set Status Trigger")
                .startAt(Date.from(startAt.toInstant()))
                .withSchedule(SimpleScheduleBuilder.simpleSchedule().withMisfireHandlingInstructionFireNow())
                .build();
    }
}
