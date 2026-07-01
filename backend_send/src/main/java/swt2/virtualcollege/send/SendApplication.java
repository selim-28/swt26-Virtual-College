// "package" sagt, in welchem Ordner/Schublade diese Datei liegt.
// Der Name muss zum Ordnerpfad passen: .../swt2/virtualcollege/send
package swt2.virtualcollege.send;

// "import" holt fertigen Code von Spring Boot herein.
// SpringApplication ist die Klasse, die unseren Server startet.
import org.springframework.boot.SpringApplication;
// SpringBootApplication ist die "Markierung", die aus dieser Klasse eine Spring-Boot-App macht.
import org.springframework.boot.autoconfigure.SpringBootApplication;

/*
 * Diese Klasse ist der STARTPUNKT des simulierten Fremdsystems
 * (ÖPNV / Studierendenwerk). Es stellt Daten bereit, die es spaeter
 * ueber MQTT an das Application-Backend (backend_receive) verschickt.
 */

// @SpringBootApplication: Hinweis an Spring Boot, dass das die Haupt-App ist.
@SpringBootApplication
// Klassen-Definition. Der Name muss zur Datei SendApplication.java passen.
public class SendApplication {

    // "main" laeuft als erstes beim Start des Programms.
    public static void main(String[] args) {
        // Startet den kompletten Web-Server dieses Backends.
        SpringApplication.run(SendApplication.class, args);
    }
}
