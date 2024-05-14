package gadgetarium.api;

import gadgetarium.dto.response.HttpResponse;
import gadgetarium.dto.response.OrderNumber;
import gadgetarium.dto.response.OrderOverViewResponse;
import gadgetarium.enums.Payment;
import gadgetarium.services.PaymentService;
import io.swagger.v3.oas.annotations.Operation;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

@Slf4j
@RestController
@RequestMapping("/api/payment")
@RequiredArgsConstructor
public class PaymentApi {

    private final PaymentService paymentService;

    @PreAuthorize("hasAuthority('USER')")
    @Operation(description = "Авторизация: USER", summary = "способ оплаты")
    @PostMapping("/payment-type/{orderId}")
    public HttpResponse paymentMethod(@RequestParam Payment payment,
                                      @PathVariable Long orderId) {
        return paymentService.paymentMethod(payment, orderId);
    }

    @PreAuthorize("hasAuthority('USER')")
    @Operation(description = "Авторизация: USER", summary = "проверка валидности карты")
    @PostMapping("/validate-card")
    public ResponseEntity<String> validateCard(@RequestParam String nonce,
                                               @RequestParam String cardholderName,
                                               @RequestParam String customerId) {
        return paymentService.validateCard(nonce, cardholderName, customerId);
    }

    @PreAuthorize("hasAuthority('USER')")
    @Operation(description = "Авторизация: USER", summary = "оплата")
    @PostMapping("/confirm-payment/{orderId}")
    public ResponseEntity<String> confirmPayment(@RequestParam String paymentMethodNonce,
                                                 @PathVariable Long orderId,
                                                 @RequestParam String customerId) {
       return paymentService.confirmPayment(paymentMethodNonce, orderId, customerId);
    }

    @PreAuthorize("hasAuthority('USER')")
    @Operation(description = "Авторизация: USER", summary = "get info of order")
    @PostMapping("/order-view/{orderId}")
    public OrderOverViewResponse orderView(@PathVariable Long orderId) {
        return paymentService.orderView(orderId);
    }

    @PreAuthorize("hasAuthority('USER')")
    @Operation(description = "Авторизация: USER", summary = "get of order number")
    @PostMapping("/order-number/{orderId}")
    public OrderNumber orderNumberInfo(@PathVariable Long orderId) {
        return paymentService.orderNumberInfo(orderId);
    }
}
