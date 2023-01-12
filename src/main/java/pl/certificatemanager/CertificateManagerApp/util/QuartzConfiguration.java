package pl.certificatemanager.CertificateManagerApp.util;

import org.springframework.boot.autoconfigure.EnableAutoConfiguration;
import org.springframework.boot.autoconfigure.quartz.QuartzDataSource;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.boot.jdbc.DataSourceBuilder;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

import javax.sql.DataSource;

@Configuration
@EnableAutoConfiguration
public class QuartzConfiguration {
    @Bean
    @QuartzDataSource
    @ConfigurationProperties(prefix = "spring.second-datasource")
    public DataSource quartzDataSource() {
        return DataSourceBuilder.create().build();
    }
}
