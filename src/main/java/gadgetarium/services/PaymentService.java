package gadgetarium.services;

import com.stripe.exception.StripeException;
import gadgetarium.dto.request.IdsGadgetAndQuantityRequest;
import gadgetarium.dto.response.*;
import gadgetarium.enums.Payment;

import java.util.List;

public interface PaymentService {

    HttpResponse typeOrder(Long orderId, Payment payment);

    OrderImageResponse orderImage(Long orderId);

    OrderSuccessResponse orderSuccess(Long orderId);

    PaymentIdResponse createPayment(Long orderId, String token);

    HttpResponse confirmPayment(String paymentId, List<IdsGadgetAndQuantityRequest> request);

    OrderIdsResponse getNew();

}