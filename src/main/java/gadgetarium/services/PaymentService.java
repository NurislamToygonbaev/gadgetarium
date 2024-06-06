package gadgetarium.services;

import com.fasterxml.jackson.databind.JsonNode;
import gadgetarium.dto.response.HttpResponse;
import gadgetarium.dto.response.OrderImageResponse;
import gadgetarium.dto.response.OrderSuccessResponse;
import gadgetarium.enums.Payment;

public interface PaymentService {

    JsonNode createPayment(Long id, String currency, String paypal, String sale, String paymentDescription, String url, String url1) throws Exception;

    JsonNode executePayment(String s, String s1) throws Exception;

    HttpResponse typeOrder(Long orderId, Payment payment);

    OrderImageResponse orderImage(Long orderId);

    OrderSuccessResponse orderSuccess(Long orderId);
}