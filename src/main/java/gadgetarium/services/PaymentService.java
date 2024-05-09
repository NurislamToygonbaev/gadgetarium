package gadgetarium.services;

import gadgetarium.dto.response.HttpResponse;
import gadgetarium.dto.response.OrderNumber;
import gadgetarium.dto.response.OrderOverViewResponse;
import gadgetarium.enums.Payment;
import org.springframework.http.ResponseEntity;

public interface PaymentService {
    ResponseEntity<String> validateCard(String nonce, String cardholderName, String customerId);

    ResponseEntity<String> confirmPayment(String paymentMethodNonce, Long orderId, String customerId);

    HttpResponse paymentMethod(Payment payment, Long orderId);

    OrderOverViewResponse orderView(Long orderId);

    OrderNumber orderNumberInfo(Long orderId);
}
