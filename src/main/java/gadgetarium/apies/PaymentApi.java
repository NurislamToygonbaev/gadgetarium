package gadgetarium.apies;

import gadgetarium.dto.response.HttpResponse;
import gadgetarium.dto.response.OrderIdsResponse;
import gadgetarium.dto.response.OrderImageResponse;
import gadgetarium.dto.response.OrderSuccessResponse;
import gadgetarium.enums.Payment;
import gadgetarium.services.PaymentService;
import io.swagger.v3.oas.annotations.Operation;
import lombok.RequiredArgsConstructor;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/payment")
@RequiredArgsConstructor
public class PaymentApi {

    private final PaymentService paymentService;

    @PreAuthorize("hasAuthority('USER')")
    @Operation(summary = "Создание платежа", description = "Авторизация: ПОЛЬЗОВАТЕЛЬ")
    @PostMapping("/create/{orderId}")
    public HttpResponse createPayment(@RequestParam String token,
                                      @PathVariable Long orderId) {
        return paymentService.createPayment(orderId, token);
    }

    @PreAuthorize("hasAuthority('USER')")
    @Operation(summary = "Подтвердение платежа ", description = "Авторизация: ПОЛЬЗОВАТЕЛЬ")
    @PostMapping("/confirm")
    public HttpResponse confirmPayment(@RequestParam String paymentId) {
        return paymentService.confirmPayment(paymentId);
    }

    @PreAuthorize("hasAuthority('USER')")
    @Operation(summary = "Выбор типа оплаты", description = "Авторизация: ПОЛЬЗОВАТЕЛЬ")
    @PatchMapping("/{orderId}")
    public HttpResponse typeOrder(@PathVariable Long orderId,
                                  @RequestParam Payment payment){
        return paymentService.typeOrder(orderId, payment);
    }

    @PreAuthorize("hasAuthority('USER')")
    @Operation(summary = "получение нового заказа Айди без статуса", description = "Авторизация: ПОЛЬЗОВАТЕЛЬ")
    @GetMapping("/get-new")
    public OrderIdsResponse getNew(){
        return paymentService.getNew();
    }

    @PreAuthorize("hasAuthority('USER')")
    @Operation(summary = "образ заказа ", description = "Авторизация: ПОЛЬЗОВАТЕЛЬ")
    @GetMapping("/order/{orderId}")
    public OrderImageResponse orderImage(@PathVariable Long orderId){
        return paymentService.orderImage(orderId);
    }

    @PreAuthorize("hasAuthority('USER')")
    @Operation(summary = "заявка оформлена", description = "Авторизация: ПОЛЬЗОВАТЕЛЬ")
    @GetMapping("/{orderId}")
    public OrderSuccessResponse orderSuccess(@PathVariable Long orderId){
        return paymentService.orderSuccess(orderId);
    }

}