package gadgetarium.services;

import gadgetarium.dto.request.CarDetails;
import gadgetarium.dto.response.OrderOverViewResponse;
import gadgetarium.dto.response.StripeResponse;
import gadgetarium.enums.Payment;

public interface StripeService {
    String checkPayment(CarDetails cardDetails, Payment payment, Long orderId);

    StripeResponse createPayment(Long orderId, String token);

    OrderOverViewResponse orderOverView(Long orderId);
}
