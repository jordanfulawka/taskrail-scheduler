package ca.jordanfulawka.taskrail.rest;

import ca.jordanfulawka.taskrail.repository.JobRunRepository;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/jobs")
public class JobController {

    private final JobRunRepository repository;

    public JobController(JobRunRepository repository) {
        this.repository = repository;
    }

    record SubmitRequest(String jobType, String payload) {};

    @PostMapping
    public Long submit(@RequestBody SubmitRequest req) {
        return repository.insert(req.jobType(), req.payload());
    }


}
