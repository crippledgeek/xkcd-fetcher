package se.disabledsecurity.xkcd.fetcher.configuration;

import lombok.extern.slf4j.Slf4j;
import org.jetbrains.annotations.NotNull;
import org.quartz.Scheduler;
import org.quartz.SchedulerException;
import org.springframework.context.ApplicationListener;
import org.springframework.context.event.ContextClosedEvent;
import org.springframework.core.annotation.Order;
import org.springframework.stereotype.Component;
import org.springframework.transaction.support.TransactionTemplate;
import se.disabledsecurity.xkcd.fetcher.exception.SchedulerCleanupException;

import java.util.Objects;

@Component
@Slf4j
@Order(100)
public class QuartzShutdownHandler implements ApplicationListener<ContextClosedEvent> {

    private final Scheduler scheduler;
    private final TransactionTemplate transactionTemplate;

    public QuartzShutdownHandler(Scheduler scheduler, TransactionTemplate transactionTemplate) {
        this.scheduler = Objects.requireNonNull(scheduler, "Scheduler cannot be null");
        this.transactionTemplate = Objects.requireNonNull(transactionTemplate, "TransactionTemplate cannot be null");
    }

    @Override
    public void onApplicationEvent(@NotNull ContextClosedEvent event) {
        log.info("Starting Quartz cleanup on context closed...");

        try {
            transactionTemplate.executeWithoutResult(status -> {
                try {
                    if (isSchedulerOperational()) {
                        scheduler.clear();
                        log.info("Quartz triggers cleared during shutdown");
                    } else {
                        log.info("Scheduler not operational, skipping clear operation");
                    }
                } catch (SchedulerException e) {
                    log.error("Error clearing Quartz triggers within transaction", e);
                    if (Objects.nonNull(status)) {
                        status.setRollbackOnly();
                    }
                    throw new SchedulerCleanupException("Failed to clear Quartz triggers during shutdown", e);
                }
            });
        } catch (Exception e) {
            log.error("Transaction execution failed during Quartz cleanup", e);
        }
    }

    private boolean isSchedulerOperational() throws SchedulerException {
        return scheduler.isStarted() || scheduler.isInStandbyMode();
    }
}