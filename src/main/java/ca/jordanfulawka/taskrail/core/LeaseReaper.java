package ca.jordanfulawka.taskrail.core;

import ca.jordanfulawka.taskrail.persistence.JobRepository;

import java.util.concurrent.Executors;
import java.util.concurrent.ScheduledExecutorService;

public class LeaseReaper {

    private final JobRepository jobRepository;
    private final ScheduledExecutorService scheduler = Executors.newScheduledThreadPool(1);

    public LeaseReaper(JobRepository jobRepository) {
        this.jobRepository = jobRepository;
    }

    public void start() {
        scheduler.scheduleWithFixedDelay()
    }


}
