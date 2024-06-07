package gadgetarium.apies;

import com.fasterxml.jackson.databind.JsonNode;
import gadgetarium.dto.request.PaymentExecutionRequest;
import gadgetarium.dto.request.PaymentRequest;
import gadgetarium.dto.response.HttpResponse;
import gadgetarium.dto.response.OrderImageResponse;
import gadgetarium.dto.response.OrderSuccessResponse;
import gadgetarium.enums.Payment;
import gadgetarium.services.PaymentService;
import io.swagger.v3.oas.annotations.Operation;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/paypal")
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

    @Operation(summary = "Выбор типа оплаты", description = "Авторизация: ПОЛЬЗОВАТЕЛЬ")
    @PatchMapping("/{orderId}")
    public HttpResponse typeOrder(@PathVariable Long orderId,
                                  @RequestParam Payment payment){
        return paymentService.typeOrder(orderId, payment);
    }

    @Operation(summary = "образ заказа ", description = "Авторизация: ПОЛЬЗОВАТЕЛЬ")
    @GetMapping("/order/{orderId}")
    public OrderImageResponse orderImage(@PathVariable Long orderId){
        return paymentService.orderImage(orderId);
    }

    @Operation(summary = "заявка оформлена", description = "Авторизация: ПОЛЬЗОВАТЕЛЬ")
    @GetMapping("/{orderId}")
    public OrderSuccessResponse orderSuccess(@PathVariable Long orderId){
        return paymentService.orderSuccess(orderId);
    }

}