package ca.jordanfulawka.taskrail.config;

import ca.jordanfulawka.taskrail.core.HandlerRegistry;
import ca.jordanfulawka.taskrail.core.Worker;
import ca.jordanfulawka.taskrail.handlers.FlakyHandler;
import ca.jordanfulawka.taskrail.handlers.JobHandler;
import ca.jordanfulawka.taskrail.handlers.SleepHandler;
import ca.jordanfulawka.taskrail.persistence.JobRepository;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

import java.util.List;

@Configuration
public class TaskrailConfig {

    @Bean
    public HandlerRegistry handlerRegistry(List <JobHandler> handlerList) {
        return new HandlerRegistry(handlerList);
    }

    @Bean
    public SleepHandler sleepHandler() {
        return new SleepHandler();
    }

    @Bean
    public FlakyHandler flakyHandler() {
        return new FlakyHandler();
    }

    @Bean(initMethod = "start", destroyMethod = "stop")
    public Worker worker(JobRepository jobRepository, HandlerRegistry handlerRegistry) {
        return new Worker(jobRepository, handlerRegistry, 3);
    }


}
