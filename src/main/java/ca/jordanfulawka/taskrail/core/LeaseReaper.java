package ca.jordanfulawka.taskrail.core;

import ca.jordanfulawka.taskrail.persistence.JobRepository;

import java.util.List;
import java.util.concurrent.Executors;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.TimeUnit;

public class LeaseReaper {

    private final JobRepository jobRepository;
    private final ScheduledExecutorService scheduler = Executors.newScheduledThreadPool(1);

    public LeaseReaper(JobRepository jobRepository) {
        this.jobRepository = jobRepository;
    }

    public void start() {
        scheduler.scheduleWithFixedDelay(this::reap, 1, 15, TimeUnit.SECONDS);
    }

    public void stop() {
        scheduler.shutdown();
    }

    private void reap() {
        try {
            List<Long> expiredIds = jobRepository.reapRunningJobs();
            if(!expiredIds.isEmpty()) {
                System.out.println("Successfully reaped " + expiredIds.size() + " jobs: " + expiredIds);
            }
        } catch(Exception e) {
            System.err.println("Lease reaper failed: " + e.getMessage());
        }
    }


}
