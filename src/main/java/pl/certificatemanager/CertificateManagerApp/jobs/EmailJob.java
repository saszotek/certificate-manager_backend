package pl.certificatemanager.CertificateManagerApp.jobs;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.quartz.JobDataMap;
import org.quartz.JobExecutionContext;
import org.springframework.scheduling.quartz.QuartzJobBean;
import org.springframework.stereotype.Component;
import pl.certificatemanager.CertificateManagerApp.util.MailUtil;

@Component
@RequiredArgsConstructor
@Slf4j
public class EmailJob extends QuartzJobBean {
    private final MailUtil mailUtil;

    @Override
    protected void executeInternal(JobExecutionContext jobExecutionContext) {
        JobDataMap jobDataMap = jobExecutionContext.getMergedJobDataMap();

        String subject = jobDataMap.getString("subject");
        String body = jobDataMap.getString("body");
        String recipientEmail = jobDataMap.getString("email");

        mailUtil.sendMail(recipientEmail, subject, body);
    }
}
