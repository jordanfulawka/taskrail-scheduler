package ca.jordanfulawka.taskrail;

import org.springframework.boot.SpringApplication;

public class TestTaskrailApplication {

	public static void main(String[] args) {
		SpringApplication.from(TaskrailApplication::main).with(TestcontainersConfiguration.class).run(args);
	}

}
