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
import org.springframework.web.bind.annotation.*;
import org.springframework.web.servlet.view.RedirectView;

@RestController
@RequestMapping("/api/paypal")
@RequiredArgsConstructor
public class PaymentApi {

    private final PaymentService paymentService;

    @GetMapping("/create")
    public RedirectView createPayment() throws PayPalRESTException {
        String cancelUrl = "";
        String successUrl = "";
        com.paypal.api.payments.Payment payment = paymentService.createPayment(
                10.0,
                "USD",
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