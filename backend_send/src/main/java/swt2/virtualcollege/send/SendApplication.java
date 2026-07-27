package swt2.virtualcollege.send;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;

/**
 * Startpunkt des simulierten Fremdsystems (OePNV / Studierendenwerk).
 * Erzeugt Datensaetze und verschickt sie ueber MQTT an backend_receive.
 */
@SpringBootApplication
public class SendApplication {

    public static void main(String[] args) {
        SpringApplication.run(SendApplication.class, args);
    }
}
