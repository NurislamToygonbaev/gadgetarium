package gadgetarium.services;

import com.stripe.exception.StripeException;
import gadgetarium.dto.response.HttpResponse;
import gadgetarium.dto.response.OrderIdsResponse;
import gadgetarium.dto.response.OrderImageResponse;
import gadgetarium.dto.response.OrderSuccessResponse;
import gadgetarium.enums.Payment;

public interface PaymentService {

    HttpResponse typeOrder(Long orderId, Payment payment);

    OrderImageResponse orderImage(Long orderId);

    OrderSuccessResponse orderSuccess(Long orderId);

    HttpResponse createPayment(Long orderId, String token);

    HttpResponse confirmPayment(String paymentId);

    OrderIdsResponse getNew();

}