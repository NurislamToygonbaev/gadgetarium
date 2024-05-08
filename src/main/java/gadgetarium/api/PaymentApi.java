package gadgetarium.api;

import gadgetarium.dto.request.CarDetails;
import gadgetarium.dto.response.OrderOverViewResponse;
import gadgetarium.dto.response.StripeResponse;
import gadgetarium.enums.Payment;
import gadgetarium.services.StripeService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.web.bind.annotation.*;

@Slf4j
@RestController
@RequestMapping("/api/payment")
@RequiredArgsConstructor
public class PaymentApi {

    private final StripeService stripeService;

    @PostMapping("/check-payment/{orderId}")
    public String checkPayment(@RequestBody @Valid CarDetails cardDetails,
                               @RequestParam Payment payment,
                               @PathVariable Long orderId) {
        return stripeService.checkPayment(cardDetails, payment, orderId);
    }

    @PostMapping("/pay/{orderId}")
    public StripeResponse createPayment(@PathVariable Long orderId,
                                        @RequestParam String token)  {
        return stripeService.createPayment(orderId, token);
    }

    @GetMapping("/oder-over-view/{orderId}")
    public OrderOverViewResponse orderOverView(@PathVariable Long orderId){
        return stripeService.orderOverView(orderId);
    }
}
