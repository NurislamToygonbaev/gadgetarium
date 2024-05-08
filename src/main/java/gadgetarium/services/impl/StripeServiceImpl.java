package gadgetarium.services.impl;

import com.stripe.exception.StripeException;
import com.stripe.model.Card;
import com.stripe.model.Charge;
import com.stripe.model.Token;
import gadgetarium.dto.request.CarDetails;
import gadgetarium.dto.response.OrderOverViewResponse;
import gadgetarium.dto.response.StripeResponse;
import gadgetarium.entities.Order;
import gadgetarium.enums.Payment;
import gadgetarium.exceptions.NotFoundException;
import gadgetarium.exceptions.PaymentProcessingException;
import gadgetarium.repositories.OrderRepository;
import gadgetarium.services.StripeService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

import java.math.BigDecimal;
import java.util.HashMap;
import java.util.Map;

@Slf4j
@Service
@RequiredArgsConstructor
public class StripeServiceImpl implements StripeService {

    private final OrderRepository orderRepo;

    @Override
    public String checkPayment(CarDetails cardDetails, Payment payment, Long orderId) {
        Order order = orderRepo.getOrderById(orderId);
        if (!order.getPayment().equals(Payment.PAYMENT_BY_CARD)){
            throw new NotFoundException();
        }
        try {
            Token token = Token.create(params(cardDetails));

            Card stripeCard = retrieveCardFromStripe(token.getId());

            if (stripeCard != null) {
                return token.getId();
            } else {
                return "Card does not exist in Stripe system";
            }
        } catch (StripeException e) {
            log.error("Error processing payment", e);
            return "Error processing payment: " + e.getMessage();
        }
    }

    @Override
    public StripeResponse createPayment(Long orderId, String token) {
        Order order = orderRepo.getOrderById(orderId);
        try {
            Charge charge = Charge.create(params(orderId, token));

            if (charge.getStatus().equals("succeeded")) {
                return StripeResponse.builder()
                        .message("Payment success")
                        .number(order.getNumber())
                        .localDate(order.getCreatedAt())
                        .email(order.getUser().getEmail())
                        .build();
            }else {
                return StripeResponse.builder()
                        .message("Payment failed")
                        .build();
            }
        } catch (StripeException e) {
            throw new PaymentProcessingException("Error processing payment: " + e.getMessage());
        }
    }

    @Override
    public OrderOverViewResponse orderOverView(Long orderId) {
        Order order = orderRepo.getOrderById(orderId);

        return OrderOverViewResponse.builder()
                .price(order.getPrice())
                .delivery(order.getUser().getAddress())
                .payment(order.getPayment())
                .build();
    }

    private Map<String, Object> params(CarDetails cardDetails) {
        Map<String, Object> params = new HashMap<>();
        params.put("card", cardDetails.toMap());
        return params;
    }

    private Map<String, Object> params(Long orderId, String token) {
        Order order = orderRepo.getOrderById(orderId);
        Map<String, Object> params = new HashMap<>();
        params.put("amount", order.getPrice().multiply(BigDecimal.valueOf(100)).intValue());
        params.put("currency", "usd");
        params.put("source", token);
        return params;
    }

    private Card retrieveCardFromStripe(String tokenId) throws StripeException {
        Token token = Token.retrieve(tokenId);
        return token.getCard();
    }
}
