package ca.jordanfulawka.taskrail.core;

import ca.jordanfulawka.taskrail.persistence.JobRepository;

import java.net.InetAddress;
import java.util.Optional;
import java.util.UUID;
import java.util.concurrent.*;

public class Worker {

    private final JobRepository jobRepository;
    private final HandlerRegistry handlerRegistry;
    private final ScheduledExecutorService scheduler = Executors.newScheduledThreadPool(1);
    private final ExecutorService jobPool;
    private final Semaphore semaphore;
    private String workerName;

    public Worker(JobRepository jobRepository, HandlerRegistry handlerRegistry, int N) {
        this.jobRepository = jobRepository;
        this.handlerRegistry = handlerRegistry;
        this.semaphore = new Semaphore(N);
        this.jobPool = Executors.newFixedThreadPool(N);
        try {
            this.workerName = InetAddress.getLocalHost().getHostName();
        } catch (Exception e) {
            this.workerName = String.valueOf(UUID.randomUUID());
        }
    }

    public void start() {
        scheduler.scheduleWithFixedDelay(this::poll, 1000, 1000, TimeUnit.MILLISECONDS);
    }

    public void stop() {

        try {
            scheduler.shutdown();
            jobPool.shutdown();
            if(!jobPool.awaitTermination(30, TimeUnit.SECONDS)) {
                jobPool.shutdownNow();
            }
        } catch (InterruptedException e) {
            jobPool.shutdownNow();
            Thread.currentThread().interrupt();
        }
    }

    private void poll() {

        try {
            if(semaphore.tryAcquire()) {
                Optional<Job> job = jobRepository.claimNext(workerName);
                if(job.isPresent()) {
                    try {
                        jobPool.execute(() -> jobHandler(job.get()));
                    } catch (RuntimeException e) {
                        System.out.println(e.getMessage());
                    }
                } else {
                    semaphore.release();
                }
            }
        } catch (Exception e) {
            semaphore.release();
            e.printStackTrace();
        }
    }

    private void jobHandler(Job job) {
        try {
            handlerRegistry.get(job.type()).handle(job.payload());
            jobRepository.markSucceeded(job.id(), workerName);
            System.out.println("ID " + job.id() + ": Job of type " + job.type() + " has completed");
        } catch (Exception e) {
//            System.err.println("Job " + job.id() + " failed: " + e.getMessage());
            System.err.println("ID " + job.id() + ": Job of type " + job.type() + " has failed. " + e.getMessage());
            jobRepository.markFailed(job.id(), e.getMessage(), workerName);
        } finally {
            semaphore.release();
        }
    }


}
