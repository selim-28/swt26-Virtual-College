package swt2.virtualcollege.send;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;

@SpringBootApplication
public class SendApplication {

    public static void main(String[] args) {
        System.out.println("Virtual College - Backend Send");
        SpringApplication.run(SendApplication.class, args);
    }
}