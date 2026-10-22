package ca.jordanfulawka.taskrail.api;

import ca.jordanfulawka.taskrail.core.HandlerRegistry;
import ca.jordanfulawka.taskrail.core.Job;
import ca.jordanfulawka.taskrail.core.JobStatus;
import ca.jordanfulawka.taskrail.handlers.JobHandler;
import ca.jordanfulawka.taskrail.persistence.JobRepository;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.Optional;

@RestController
@RequestMapping("/jobs")
public class JobController {

    private final JobRepository jobRepository;
    private final HandlerRegistry handlerRegistry;

    public JobController(JobRepository jobRepository, HandlerRegistry handlerRegistry) {
        this.jobRepository = jobRepository;
        this.handlerRegistry = handlerRegistry;
    }

    @PostMapping("")
    public ResponseEntity<Long> addJob(@RequestBody JobRequestDTO job) {
        if(!handlerRegistry.supports(job.type())) {
            return ResponseEntity.badRequest().build();
        }
        JobHandler jobHandler = handlerRegistry.get(job.type());
        if(!jobHandler.validate(job.payload())) {
            return ResponseEntity.badRequest().build();
        }
        
        Job newJob = jobRepository.insert(job.type(), job.payload(), job.runAt());
        return ResponseEntity.ok(newJob.id());
    }

    @GetMapping("")
    public ResponseEntity<List<Job>> getJobsByStatus(@RequestParam String status) {
        if(!JobStatus.isValidJobStatus(status)) {
            return ResponseEntity.badRequest().build();
        }

        List<Job> jobs = jobRepository.findByStatus(JobStatus.valueOf(status.toUpperCase()));
        return ResponseEntity.ok(jobs);
    }

    @GetMapping("/{id}")
    public ResponseEntity<Job> getJob(@PathVariable long id) {
        Optional<Job> job = jobRepository.findById(id);
        return job.map(ResponseEntity::ok).orElseGet(() -> ResponseEntity.notFound().build());
    }

}
