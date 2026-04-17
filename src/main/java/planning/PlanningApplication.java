package planning;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;

@SpringBootApplication(scanBasePackages = {"planning", "com.planning.microplanning"})
public class PlanningApplication {

	public static void main(String[] args) {
		SpringApplication.run(PlanningApplication.class, args);
	}

}
