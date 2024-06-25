package gadgetarium.services;

import com.paypal.base.rest.PayPalRESTException;
import gadgetarium.dto.response.HttpResponse;
import gadgetarium.dto.response.OrderImageResponse;
import gadgetarium.dto.response.OrderSuccessResponse;
import gadgetarium.enums.Payment;

public interface PaymentService {

    com.paypal.api.payments.Payment createPayment(
            Long orderId,
            String currency,
            String method,
            String intent,
            String description,
            String cancelUrl,
            String successUrl
    ) throws PayPalRESTException;

    com.paypal.api.payments.Payment executePayment(
            String paymentId,
            String payerId
    ) throws PayPalRESTException;

    HttpResponse typeOrder(Long orderId, Payment payment);

    OrderImageResponse orderImage(Long orderId);

    OrderSuccessResponse orderSuccess(Long orderId);
}