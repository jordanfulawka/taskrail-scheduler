package ca.jordanfulawka.taskrail.core;

import ca.jordanfulawka.taskrail.exceptions.PermanentJobException;
import ca.jordanfulawka.taskrail.exceptions.RetryableJobException;
import ca.jordanfulawka.taskrail.persistence.JobRepository;
import ca.jordanfulawka.taskrail.persistence.JobRunRepository;

import java.net.InetAddress;
import java.util.Optional;
import java.util.UUID;
import java.util.concurrent.*;

public class Worker {

    private final JobRepository jobRepository;
    private final JobRunRepository jobRunRepository;
    private final HandlerRegistry handlerRegistry;
    private final ScheduledExecutorService scheduler = Executors.newScheduledThreadPool(1);
    private final ExecutorService jobPool;
    private final Semaphore semaphore;
    private String workerName;

    public Worker(JobRepository jobRepository, JobRunRepository jobRunRepository,HandlerRegistry handlerRegistry, int N) {
        this.jobRepository = jobRepository;
        this.jobRunRepository = jobRunRepository;
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
        long jobRunId = -1;
        try {
            jobRunId = jobRunRepository.startRun(job.id(), job.attempts(), workerName);
            handlerRegistry.get(job.type()).handle(job.payload());
            jobRepository.markSucceeded(job.id(), workerName, jobRunId);
            System.out.println("ID " + job.id() + ": Job of type " + job.type() + " has completed");
        } catch (PermanentJobException e) {
            System.err.println("ID " + job.id() + ": Job of type " + job.type() + " has failed permanently. " + e.getMessage());
            jobRepository.markFailed(job.id(), e.getMessage(), workerName, jobRunId);
        } catch (RetryableJobException e) {
            System.err.println("ID " + job.id() + ": Job of type " + job.type() + " has failed temporarily. " + e.getMessage());
            jobRepository.markPending(job.id(), e.getMessage(), workerName, jobRunId);
        } catch (Exception e) {
            System.err.println("ID " + job.id() + ": Job of type " + job.type() + " has had an unknown failure " + e.getMessage());
            jobRepository.markPending(job.id(), e.getMessage(), workerName, jobRunId);
        }  finally {
            semaphore.release();
        }
    }


}
