package swt2.virtualcollege.receive;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;

/**
 * Startpunkt des Application Servers. Liefert dem Frontend die REST-Endpunkte
 * und empfaengt die Daten des Fremdsystems ueber MQTT.
 */
@SpringBootApplication
public class ReceiveApplication {

    public static void main(String[] args) {
        SpringApplication.run(ReceiveApplication.class, args);
    }
}
