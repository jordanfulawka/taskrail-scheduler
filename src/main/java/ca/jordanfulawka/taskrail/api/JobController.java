package ca.jordanfulawka.taskrail.api;

import ca.jordanfulawka.taskrail.core.Job;
import ca.jordanfulawka.taskrail.persistence.JobRepository;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/jobs")
public class JobController {

    private final JobRepository jobRepository;

    public JobController(JobRepository jobRepository) {
        this.jobRepository = jobRepository;
    }

    @PostMapping("")
    public void addJob(@RequestBody JobRequestDTO job) {
        Job newJob = jobRepository.insert(job.type(), job.payload(), job.runAt());
        System.out.println(newJob);
    }

}
