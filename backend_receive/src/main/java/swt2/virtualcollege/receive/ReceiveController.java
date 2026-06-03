package swt2.virtualcollege.receive;

import org.springframework.integration.annotation.ServiceActivator;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.Map;
import java.util.concurrent.atomic.AtomicReference;

@RestController
@RequestMapping("/api/receive")
public class ReceiveController {

    private final AtomicReference<String> lastMessage = new AtomicReference<>("no MQTT message received yet");

    @GetMapping("/status")
    public ResponseEntity<Map<String, String>> status() {
        return ResponseEntity.ok(Map.of("status", "running", "module", "backend-receive"));
    }

    @GetMapping("/last")
    public ResponseEntity<Map<String, String>> lastMessage() {
        return ResponseEntity.ok(Map.of("topic", "virtualcollege/topic", "message", lastMessage.get()));
    }

    @ServiceActivator(inputChannel = "mqttInputChannel")
    public void handleMqttMessage(String payload) {
        lastMessage.set(payload);
    }
}
