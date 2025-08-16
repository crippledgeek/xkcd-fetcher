package se.disabledsecurity.xkcd.fetcher.configuration;

import org.quartz.JobBuilder;
import org.quartz.JobDetail;
import org.quartz.Trigger;
import org.quartz.TriggerBuilder;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import se.disabledsecurity.xkcd.fetcher.common.XkcdProperties;
import se.disabledsecurity.xkcd.fetcher.jobs.ComicsJob;


import java.time.Clock;
import java.time.LocalDateTime;
import java.time.ZoneOffset;
import java.util.Date;

import static org.quartz.SimpleScheduleBuilder.simpleSchedule;

@Configuration(proxyBeanMethods = false)
public class SchedulerConfiguration {

    private final XkcdProperties properties;

    public SchedulerConfiguration(XkcdProperties properties) {
        this.properties = properties;
    }

    @Bean
    public JobDetail comicsJobDetail() {
        return JobBuilder
                .newJob(ComicsJob.class)
                .withIdentity("getComicsJob")
                .storeDurably()
                .build();
    }

    @Bean
    public Trigger comicJobTrigger(JobDetail comicsJobDetail) {
        return TriggerBuilder
                .newTrigger()
                .withIdentity(ComicsJob.class.getSimpleName())
                .startAt(Date.from(
                        LocalDateTime.now(Clock.system(ZoneOffset.UTC))
                                .plusMinutes(properties.getScheduler().getInitialDelayInMinutes())
                                .atZone(ZoneOffset.UTC)
                                .toInstant()))
                .forJob(comicsJobDetail)
                .withSchedule(simpleSchedule()
                        .withIntervalInMinutes(properties.getScheduler()
                                .getIntervalInMinutes())
                        .repeatForever())
                .build();
    }


}
