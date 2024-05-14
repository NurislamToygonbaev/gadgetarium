package gadgetarium.services.impl;

import com.braintreegateway.*;
import gadgetarium.dto.response.HttpResponse;
import gadgetarium.dto.response.OrderNumber;
import gadgetarium.dto.response.OrderOverViewResponse;
import gadgetarium.entities.Order;
import gadgetarium.enums.Payment;
import gadgetarium.repositories.OrderRepository;
import gadgetarium.services.PaymentService;
import jakarta.transaction.Transactional;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Service;

@Service
@RequiredArgsConstructor
public class PaymentServiceImpl implements PaymentService {

    private final OrderRepository orderRepo;
    private final BraintreeGateway braintreeGateway;

    @Override
    @Transactional
    public ResponseEntity<String> validateCard(String nonce, String cardholderName, String customerId) {
        PaymentMethodRequest paymentMethodRequest = new PaymentMethodRequest()
                .paymentMethodNonce(nonce)
                .cardholderName(cardholderName)
                .customerId(customerId);

        Result<? extends PaymentMethod> result = braintreeGateway.paymentMethod().create(paymentMethodRequest);

        if (result.isSuccess()) {
            PaymentMethod paymentMethod = result.getTarget();
            return ResponseEntity.ok("Card is valid. PaymentMethodToken: " + paymentMethod.getToken());
        } else {
            return ResponseEntity.badRequest().body("Card validation failed: " + result.getMessage());
        }
    }

    @Override
    @Transactional
    public ResponseEntity<String> confirmPayment(String paymentMethodNonce, Long orderId, String customerId) {
        Order order = orderRepo.getOrderById(orderId);
        TransactionRequest request = new TransactionRequest()
                .amount(order.getTotalPrice())
                .paymentMethodNonce(paymentMethodNonce)
                .customerId(customerId)
                .options()
                .submitForSettlement(true)
                .done();

        request.customer().firstName(order.getUser().getFirstName());
        request.customer().lastName(order.getUser().getLastName());

        Result<Transaction> result = braintreeGateway.transaction().sale(request);

        if (result.isSuccess()) {
            return ResponseEntity.ok("Payment successful! Transaction ID: " + result.getTarget().getId());
        } else {
            return ResponseEntity.badRequest().body("Payment failed: " + result.getMessage());
        }
    }


    @Override
    @Transactional
    public HttpResponse paymentMethod(Payment payment, Long orderId) {
        Order order = orderRepo.getOrderById(orderId);
        order.setPayment(payment);
        return HttpResponse.builder()
                .status(HttpStatus.OK)
                .message("success")
                .build();
    }

    @Override
    public OrderOverViewResponse orderView(Long orderId) {
        Order order = orderRepo.getOrderById(orderId);
        return OrderOverViewResponse.builder()
                .price(order.getTotalPrice())
                .delivery(order.getUser().getAddress())
                .payment(order.getPayment())
                .build();
    }

    @Override
    public OrderNumber orderNumberInfo(Long orderId) {
        Order order = orderRepo.getOrderById(orderId);
        return OrderNumber.builder()
                .number(order.getNumber())
                .date(order.getCreatedAt())
                .email(order.getUser().getEmail())
                .build();
    }
}
