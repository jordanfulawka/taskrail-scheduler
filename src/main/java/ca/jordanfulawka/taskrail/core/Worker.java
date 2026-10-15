package ca.jordanfulawka.taskrail.core;

import ca.jordanfulawka.taskrail.persistence.JobRepository;

import java.util.concurrent.Executors;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.TimeUnit;

public class Worker {

    private final JobRepository jobRepository;
    private final HandlerRegistry handlerRegistry;
    private final ScheduledExecutorService scheduler = Executors.newScheduledThreadPool(1);

    public Worker(JobRepository jobRepository, HandlerRegistry handlerRegistry) {
        this.jobRepository = jobRepository;
        this.handlerRegistry = handlerRegistry;
    }

    public void start() {
        scheduler.scheduleWithFixedDelay(this::poll, 1000, 1000, TimeUnit.MILLISECONDS);
    }

    public void stop() {
        scheduler.shutdown();
    }

    private void poll() {

        try {
            jobRepository.claimNext().ifPresent(job -> {
                try {
                    handlerRegistry.get(job.type()).handle(job.payload());
                    jobRepository.markSucceeded(job.id());
                } catch (Exception e) {
                    System.err.println("Job " + job.id() + " failed: " + e.getMessage());
                    jobRepository.markFailed(job.id());
                }
            });
        } catch (Exception e) {
//            System.out.println("Error!!");
            e.printStackTrace();
        }
    }


}
