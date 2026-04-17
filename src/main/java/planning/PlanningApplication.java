package planning;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.AutoConfigurationPackage;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.data.jpa.repository.config.EnableJpaRepositories;

@SpringBootApplication(scanBasePackages = {"planning", "com.planning.microplanning"})
@AutoConfigurationPackage(basePackages = {"planning", "com.planning.microplanning"})
@EnableJpaRepositories(basePackages = {"com.planning.microplanning.repository"})
public class PlanningApplication {

	public static void main(String[] args) {
		SpringApplication.run(PlanningApplication.class, args);
	}

}
