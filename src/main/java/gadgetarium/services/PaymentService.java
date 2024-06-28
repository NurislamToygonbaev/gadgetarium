package gadgetarium.services;

import gadgetarium.dto.response.HttpResponse;
import gadgetarium.dto.response.OrderImageResponse;
import gadgetarium.dto.response.OrderSuccessResponse;
import gadgetarium.enums.Payment;

public interface PaymentService {

    HttpResponse typeOrder(Long orderId, Payment payment);

    OrderImageResponse orderImage(Long orderId);

    OrderSuccessResponse orderSuccess(Long orderId);

    HttpResponse createPayment(Long orderId, String token);

    HttpResponse confirmPayment(String paymentId);
}