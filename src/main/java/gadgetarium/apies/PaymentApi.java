package gadgetarium.apies;

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
@CrossOrigin(origins = "*", maxAge = 3600)
public class PaymentApi {

    private final PaymentService paymentService;

    @PreAuthorize("hasAuthority('USER')")
    @Operation(description = "Авторизация: ПОЛЬЗОВАТЕЛЬ", summary = "Способ оплаты")
    @PostMapping("/type/{orderId}")
    public HttpResponse paymentMethod(@RequestParam Payment payment,
                                      @PathVariable Long orderId) {
        return paymentService.paymentMethod(payment, orderId);
    }

    @PreAuthorize("hasAuthority('USER')")
    @Operation(description = "Авторизация: ПОЛЬЗОВАТЕЛЬ", summary = "Проверка валидности карты")
    @GetMapping("/validate")
    public ResponseEntity<String> validateCard(@RequestParam String nonce,
                                               @RequestParam String cardholderName,
                                               @RequestParam String customerId) {
        return paymentService.validateCard(nonce, cardholderName, customerId);
    }

    @PreAuthorize("hasAuthority('USER')")
    @Operation(description = "Авторизация: ПОЛЬЗОВАТЕЛЬ", summary = "Оплата")
    @PostMapping("/payment/{orderId}")
    public ResponseEntity<String> confirmPayment(@RequestParam String paymentMethodNonce,
                                                 @PathVariable Long orderId,
                                                 @RequestParam String customerId) {
       return paymentService.confirmPayment(paymentMethodNonce, orderId, customerId);
    }

    @PreAuthorize("hasAuthority('USER')")
    @Operation(description = "Авторизация: ПОЛЬЗОВАТЕЛЬ", summary = "Получить информацию о заказе")
    @GetMapping("/info/{orderId}")
    public OrderOverViewResponse orderView(@PathVariable Long orderId) {
        return paymentService.orderView(orderId);
    }

    @PreAuthorize("hasAuthority('USER')")
    @Operation(description = "Авторизация: ПОЛЬЗОВАТЕЛЬ", summary = "Получить номер заказа")
    @GetMapping("/number/{orderId}")
    public OrderNumber orderNumberInfo(@PathVariable Long orderId) {
        return paymentService.orderNumberInfo(orderId);
    }
}
