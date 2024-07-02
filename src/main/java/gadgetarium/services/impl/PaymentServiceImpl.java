package gadgetarium.services.impl;

import com.stripe.exception.StripeException;
import com.stripe.model.PaymentIntent;
import com.stripe.model.PaymentMethod;
import com.stripe.param.PaymentIntentCreateParams;
import com.stripe.param.PaymentMethodCreateParams;
import gadgetarium.dto.request.IdsGadgetAndQuantityRequest;
import gadgetarium.dto.response.*;
import gadgetarium.entities.Order;
import gadgetarium.entities.SubGadget;
import gadgetarium.entities.User;
import gadgetarium.enums.Payment;
import gadgetarium.enums.RemotenessStatus;
import gadgetarium.enums.Status;
import gadgetarium.exceptions.BadRequestException;
import gadgetarium.repositories.OrderRepository;
import gadgetarium.repositories.SubGadgetRepository;
import gadgetarium.services.PaymentService;
import jakarta.transaction.Transactional;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;

import java.math.BigDecimal;
import java.util.List;

@Service
@RequiredArgsConstructor
public class PaymentServiceImpl implements PaymentService {

    private final OrderRepository orderRepo;
    private final CurrentUser currentUser;
    private final SubGadgetRepository subGadgetRepo;

    @Override
    @Transactional
    public HttpResponse typeOrder(Long orderId, String payment) {
        Payment english = Payment.fromRussian(payment);
        Order order = orderRepo.getOrderById(orderId);
        order.setPayment(english);
        orderRepo.save(order);
        return HttpResponse.builder().status(HttpStatus.OK)
                .message("success changed order with ID: " + order.getId()).build();
    }

    @Override
    public OrderImageResponse orderImage(Long orderId) {
        Order order = orderRepo.getOrderById(orderId);
        String paymentRussian = Payment.toRussian(order.getPayment().name());

        return OrderImageResponse.builder()
                .id(order.getId())
                .price(order.getTotalPrice())
                .delivery(order.getUser().getAddress())
                .payment(paymentRussian)
                .build();
    }

    @Override
    @Transactional
    public OrderSuccessResponse orderSuccess(Long orderId) {
        Order order = orderRepo.getOrderById(orderId);
        order.setStatus(Status.PENDING);
        orderRepo.save(order);
        return OrderSuccessResponse.builder()
                .number(order.getNumber())
                .createAd(String.valueOf(order.getCreatedAt()))
                .email(order.getUser().getEmail())
                .build();
    }

    @Override
    public PaymentIdResponse createPayment(Long orderId, String token) {
        Order order = orderRepo.getOrderById(orderId);
        if (order == null) {
            throw new BadRequestException("Order not found");
        }

        if (!Payment.PAYMENT_BY_CARD.equals(order.getPayment())) {
            throw new BadRequestException("Incorrect payment type");
        }

        BigDecimal totalPrice = order.getTotalPrice();
        if (totalPrice == null) {
            throw new BadRequestException("Total price not found");
        }

        long amountInCents = totalPrice.multiply(new BigDecimal(100)).longValue();
        String receiptEmail = order.getUser().getEmail();

        if (receiptEmail == null || receiptEmail.isEmpty()) {
            throw new BadRequestException("Email not found for user");
        }

        PaymentMethodCreateParams paymentMethodParams = PaymentMethodCreateParams.builder()
                .setType(PaymentMethodCreateParams.Type.CARD)
                .setCard(PaymentMethodCreateParams.Token.builder().setToken(token).build())
                .build();

        PaymentIntent intent;
        try {
            PaymentMethod paymentMethod = PaymentMethod.create(paymentMethodParams);

            PaymentIntentCreateParams paymentIntentCreateParams = PaymentIntentCreateParams.builder()
                    .setAmount(amountInCents)
                    .setPaymentMethod(paymentMethod.getId())
                    .setAutomaticPaymentMethods(PaymentIntentCreateParams.
                            AutomaticPaymentMethods
                            .builder()
                            .setEnabled(true)
                            .putExtraParam("allow_redirects", "never")
                            .build())
                    .setCurrency("kgs")
                    .build();

            intent = PaymentIntent.create(paymentIntentCreateParams);
        } catch (StripeException e) {
            throw new RuntimeException("Payment creation failed", e);
        }

        return PaymentIdResponse.builder()
                .paymentId(intent.getId())
                .httpResponse(new HttpResponse(HttpStatus.OK, "Payment created successfully."))
                .build();
    }


    @Override @Transactional
    public HttpResponse confirmPayment(String paymentId, List<IdsGadgetAndQuantityRequest> request) {
        if (paymentId == null || paymentId.isEmpty()) {
            throw new BadRequestException("Payment ID cannot be null or empty");
        }

        if (request == null || request.isEmpty()) {
            throw new BadRequestException("Request list cannot be null or empty");
        }

        for (IdsGadgetAndQuantityRequest quantityRequest : request) {
            SubGadget subGadget = subGadgetRepo.getByID(quantityRequest.id());
            int newQuantity = subGadget.getQuantity() - quantityRequest.quantity();
            if (newQuantity < 0) {
                throw new BadRequestException("Not enough stock for SubGadget with ID: " + quantityRequest.id());
            }
            if (newQuantity == 0) {
                subGadget.setRemotenessStatus(RemotenessStatus.REMOTE);
            }
            subGadget.setQuantity(newQuantity);
            subGadgetRepo.save(subGadget);
        }

        try {
            PaymentIntent paymentIntent = PaymentIntent.retrieve(paymentId);
            paymentIntent.confirm();
        } catch (StripeException e) {
            throw new BadRequestException("Payment confirmation failed: " + e.getMessage());
        }

        return new HttpResponse(HttpStatus.OK, "Payment confirmed successfully.");
    }


    @Override
    public OrderIdsResponse getNew() {
        User user = currentUser.get();
        return OrderIdsResponse.builder()
                .orderId(orderRepo.findLastByStatusIsNullAndUserId(user.getId()))
                .build();
    }
}