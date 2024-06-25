package gadgetarium.apies;

import com.paypal.api.payments.Links;
import com.paypal.base.rest.PayPalRESTException;
import gadgetarium.dto.response.HttpResponse;
import gadgetarium.dto.response.OrderImageResponse;
import gadgetarium.dto.response.OrderSuccessResponse;
import gadgetarium.enums.Payment;
import gadgetarium.services.PaymentService;
import io.swagger.v3.oas.annotations.Operation;
import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.servlet.view.RedirectView;

@RestController
@RequestMapping("/api/paypal")
@RequiredArgsConstructor
public class PaymentApi {

    private final PaymentService paymentService;

    @Value("${paypal.success-url}")
    private String successUrl;
    @Value("${paypal.cancel-url}")
    private String cancelUrl;

    @PreAuthorize("hasAuthority('USER')")
    @Operation(summary = " создание заказа ", description = "Авторизация: ПОЛЬЗОВАТЕЛЬ")
    @GetMapping("/create/{orderId}")
    public RedirectView createPayment(@PathVariable Long orderId,
                                      @RequestParam String currency
                                      ) throws PayPalRESTException {

        com.paypal.api.payments.Payment payment = paymentService.createPayment(
                orderId,
                currency,
                "paypal",
                "sale",
                "Payment description",
                cancelUrl,
                successUrl
        );
        for (Links link : payment.getLinks()) {
            if (link.getRel().equals("approval_url")){
                return new RedirectView(link.getHref());
            }
        }
        return new RedirectView("/api/payment/error");
    }

    @PreAuthorize("hasAuthority('USER')")
    @Operation(summary = " success заказа ", description = "Авторизация: ПОЛЬЗОВАТЕЛЬ")
    @GetMapping("/success")
    public String paymentSuccess(@RequestParam("paymentId") String paymentId,
                                 @RequestParam("payerId") String payerId) throws PayPalRESTException {
        com.paypal.api.payments.Payment payment = paymentService.executePayment(paymentId, payerId);
        if (payment.getState().equals("approved")){
            return "paymentSuccess";
        }
        return "paymentSuccess";
    }

    @GetMapping("/cancel")
    public String paymentCancel(){
        return "paymentCancel";
    }

    @GetMapping("/error")
    public String paymentError(){
        return "paymentError";
    }

    @PreAuthorize("hasAuthority('USER')")
    @Operation(summary = "Выбор типа оплаты", description = "Авторизация: ПОЛЬЗОВАТЕЛЬ")
    @PatchMapping("/{orderId}")
    public HttpResponse typeOrder(@PathVariable Long orderId,
                                  @RequestParam Payment payment){
        return paymentService.typeOrder(orderId, payment);
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