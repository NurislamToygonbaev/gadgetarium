package gadgetarium.services;

import com.stripe.exception.StripeException;
import gadgetarium.dto.response.*;
import gadgetarium.enums.Payment;

public interface PaymentService {

    HttpResponse typeOrder(Long orderId, Payment payment);

    OrderImageResponse orderImage(Long orderId);

    OrderSuccessResponse orderSuccess(Long orderId);

    PaymentIdResponse createPayment(Long orderId, String token);

    HttpResponse confirmPayment(String paymentId);

    OrderIdsResponse getNew();

}