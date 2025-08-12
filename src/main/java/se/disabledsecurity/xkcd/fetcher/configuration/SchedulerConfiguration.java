package se.disabledsecurity.xkcd.fetcher.configuration;

import org.quartz.JobBuilder;
import org.quartz.JobDetail;
import org.quartz.Trigger;
import org.quartz.TriggerBuilder;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import se.disabledsecurity.xkcd.fetcher.common.XkcdProperties;
import se.disabledsecurity.xkcd.fetcher.jobs.ComicsJob;


import java.time.*;
import java.time.temporal.ChronoUnit;
import java.util.Date;

import static org.quartz.SimpleScheduleBuilder.simpleSchedule;

@Configuration
public class SchedulerConfiguration {

    private final XkcdProperties properties;

    public SchedulerConfiguration(XkcdProperties properties) {
        this.properties = properties;
    }

    @Bean
    public JobDetail jobADetails() {
        return JobBuilder
                .newJob(ComicsJob.class)
                .withIdentity("getComicsJob")
                .storeDurably()
                .build();
    }

    @Bean
    public Trigger jobATrigger(JobDetail jobADetails) {
        return TriggerBuilder
                .newTrigger()
                .withIdentity(ComicsJob.class.getSimpleName())
                .startAt(Date.from(
                        LocalDateTime.now()
                                .plusMinutes(properties.getScheduler().getInitialDelayInMinutes())
                                .atZone(ZoneId.from(ZoneOffset.UTC))
                                .toInstant()
                ))
                .forJob(jobADetails)
                .withSchedule(simpleSchedule()
                        .withIntervalInMinutes(properties.getScheduler().getIntervalInMinutes())
                        .repeatForever()
                        .build()
                        .getScheduleBuilder())
                .build();
    }

}
