package gadgetarium.services;

import gadgetarium.dto.request.IdsGadgetAndQuantityRequest;
import gadgetarium.dto.response.*;

public interface PaymentService {

    HttpResponse typeOrder(Long orderId, String payment);

    OrderImageResponse orderImage(Long orderId);

    OrderSuccessResponse orderSuccess(Long orderId);

    PaymentIdResponse createPayment(Long orderId, String token);

    HttpResponse confirmPayment(String paymentId, IdsGadgetAndQuantityRequest request);

    OrderIdsResponse getNew();

}