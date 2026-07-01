package swt2.virtualcollege.send;

import org.springframework.http.ResponseEntity;
import org.springframework.messaging.MessageChannel;
import org.springframework.messaging.support.MessageBuilder;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import java.util.Map;

@RestController
@RequestMapping("/api/send")
public class SendController {

    private final MessageChannel mqttOutboundChannel;

    public SendController(MessageChannel mqttOutboundChannel) {
        this.mqttOutboundChannel = mqttOutboundChannel;
    }

    @GetMapping("/status")
    public ResponseEntity<Map<String, String>> status() {
        return ResponseEntity.ok(Map.of("status", "running", "module", "backend-send"));
    }

    @GetMapping("/publish")
    public ResponseEntity<Map<String, String>> publish(@RequestParam(defaultValue = "Hello from backend-send") String message) {
        mqttOutboundChannel.send(MessageBuilder.withPayload(message).build());
        return ResponseEntity.ok(Map.of("topic", "virtualcollege/topic", "message", message));
    }
}
