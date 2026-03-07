package uz.samtuit.maroqandObod;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.scheduling.annotation.EnableScheduling;

@SpringBootApplication
@EnableScheduling
public class MaroqandObodApplication {

	public static void main(String[] args) {
		SpringApplication.run(MaroqandObodApplication.class, args);
	}

}
