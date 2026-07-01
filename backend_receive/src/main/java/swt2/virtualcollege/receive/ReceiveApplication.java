// "package" sagt, in welchem Ordner/Schublade diese Datei liegt.
// Der Name muss zum Ordnerpfad passen: .../swt2/virtualcollege/receive
package swt2.virtualcollege.receive;

// "import" holt fertigen Code von Spring Boot herein, damit wir ihn unten benutzen koennen.
// SpringApplication ist die Klasse, die unseren Server startet.
import org.springframework.boot.SpringApplication;
// SpringBootApplication ist die "Markierung", die aus dieser Klasse eine Spring-Boot-App macht.
import org.springframework.boot.autoconfigure.SpringBootApplication;

/*
 * Diese Klasse ist der STARTPUNKT vom "Application Server"-Backend.
 * Es liefert dem Frontend ueber REST-Endpunkte Daten und empfaengt
 * spaeter die Daten des Fremdsystems ueber MQTT (Mosquitto).
 */

// @SpringBootApplication ist eine Annotation (= ein Hinweis an Spring Boot).
// Sie sagt: "Das hier ist die Haupt-App. Suche selbststaendig nach Controllern usw."
// Ohne diese Zeile wuerde kein Server starten.
@SpringBootApplication
// "public class ReceiveApplication" definiert die Klasse. Der Name MUSS
// genauso heissen wie die Datei (ReceiveApplication.java).
public class ReceiveApplication {

    // "main" ist die Methode, die als allererstes laeuft, wenn man das Programm startet.
    // So heisst der Start-Einstiegspunkt in jedem Java-Programm.
    public static void main(String[] args) {
        // Diese eine Zeile startet den kompletten Web-Server.
        // ReceiveApplication.class = "starte genau diese App".
        // args = Eingaben, die man dem Programm beim Start mitgeben kann.
        SpringApplication.run(ReceiveApplication.class, args);
    }
}
