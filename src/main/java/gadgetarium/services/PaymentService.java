package gadgetarium.services;

import com.fasterxml.jackson.databind.JsonNode;
import com.paypal.base.rest.PayPalRESTException;
import gadgetarium.dto.response.HttpResponse;
import gadgetarium.dto.response.OrderImageResponse;
import gadgetarium.dto.response.OrderSuccessResponse;
import gadgetarium.enums.Payment;

import java.math.BigDecimal;

public interface PaymentService {

    com.paypal.api.payments.Payment createPayment(
            BigDecimal total,
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