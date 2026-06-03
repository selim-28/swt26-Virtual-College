package swt2.virtualcollege.receive;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;

@SpringBootApplication
public class ReceiveApplication {

    public static void main(String[] args) {
        System.out.println("Virtual College - Backend Receive");
        SpringApplication.run(ReceiveApplication.class, args);
    }
}