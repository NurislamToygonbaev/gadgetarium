package gadgetarium.apies;

import com.fasterxml.jackson.databind.JsonNode;
import gadgetarium.dto.request.PaymentExecutionRequest;
import gadgetarium.dto.request.PaymentRequest;
import gadgetarium.services.PaymentService;
import io.swagger.v3.oas.annotations.Operation;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/paypal/orders")
@RequiredArgsConstructor
public class PaymentApi {

    private final PaymentService paymentService;

    @Operation(summary = "создание нового платежа", description = "Авторизация: ПОЛЬЗОВАТЕЛЬ")
    @PostMapping("/create-payment")
    public ResponseEntity<JsonNode> createPayment(@RequestBody PaymentRequest paymentRequest) {
        try {
            JsonNode response = paymentService.createPayment(
                    paymentRequest.id(),
                    paymentRequest.currency(),
                    "paypal",
                    "sale",
                    "Payment description",
                    "http://yourwebsite.com/payment/cancel",
                    "http://yourwebsite.com/payment/success"
            );
            return ResponseEntity.ok(response);
        } catch (Exception e) {
            e.printStackTrace();
            return ResponseEntity.status(500).body(null);
        }
    }

    @Operation(summary = "Выполнения платежа", description = "Авторизация: ПОЛЬЗОВАТЕЛЬ")
    @PostMapping("/execute-payment")
    public ResponseEntity<JsonNode> executePayment(@RequestBody PaymentExecutionRequest executionRequest) {
        try {
            JsonNode response = paymentService.executePayment(
                    executionRequest.paymentId(),
                    executionRequest.payerId());
            return ResponseEntity.ok(response);
        } catch (Exception e) {
            e.printStackTrace();
            return ResponseEntity.status(500).body(null);
        }
    }
}