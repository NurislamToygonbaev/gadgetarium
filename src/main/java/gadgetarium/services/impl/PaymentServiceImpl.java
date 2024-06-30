package gadgetarium.services.impl;

import com.stripe.exception.StripeException;
import com.stripe.model.Customer;
import com.stripe.model.CustomerSearchResult;
import com.stripe.model.PaymentIntent;
import com.stripe.model.PaymentMethod;
import com.stripe.param.PaymentIntentCreateParams;
import com.stripe.param.PaymentMethodAttachParams;
import com.stripe.param.PaymentMethodCreateParams;
import gadgetarium.dto.response.HttpResponse;
import gadgetarium.dto.response.OrderImageResponse;
import gadgetarium.dto.response.OrderSuccessResponse;
import gadgetarium.entities.Order;
import gadgetarium.entities.User;
import gadgetarium.enums.Payment;
import gadgetarium.enums.Status;
import gadgetarium.exceptions.BadRequestException;
import gadgetarium.exceptions.NotFoundException;
import gadgetarium.repositories.OrderRepository;
import gadgetarium.repositories.UserRepository;
import gadgetarium.services.PaymentService;
import jakarta.mail.MessagingException;
import jakarta.mail.internet.MimeMessage;
import jakarta.transaction.Transactional;
import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.HttpStatus;
import org.springframework.mail.javamail.JavaMailSender;
import org.springframework.mail.javamail.MimeMessageHelper;
import org.springframework.stereotype.Service;

import java.io.UnsupportedEncodingException;
import java.math.BigDecimal;
import java.net.URLEncoder;
import java.nio.charset.StandardCharsets;
import java.util.List;
import java.util.Map;

@Service
@RequiredArgsConstructor
public class PaymentServiceImpl implements PaymentService {

    private final OrderRepository orderRepo;
    private final CurrentUser currentUser;
    private final JavaMailSender javaMailSender;

    @Override
    @Transactional
    public HttpResponse typeOrder(Long orderId, Payment payment) {
        Order order = orderRepo.getOrderById(orderId);
        order.setPayment(payment);
        orderRepo.save(order);
        return HttpResponse.builder().status(HttpStatus.OK)
                .message("success changed order with ID: " + order.getId()).build();
    }

    @Override
    public OrderImageResponse orderImage(Long orderId) {
        Order order = orderRepo.getOrderById(orderId);
        return OrderImageResponse.builder()
                .id(order.getId())
                .price(order.getTotalPrice())
                .delivery(order.getUser().getAddress())
                .payment(order.getPayment())
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
    public HttpResponse createPayment(Long orderId, String token) {
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
                    .setAutomaticPaymentMethods(PaymentIntentCreateParams.AutomaticPaymentMethods.builder()
                            .setEnabled(true)
                            .build())
                    .setCurrency("kgs")
                    .setReceiptEmail(receiptEmail)
                    .build();

            intent = PaymentIntent.create(paymentIntentCreateParams);

            sendEmail(intent.getId(), receiptEmail);
        } catch (StripeException e) {
            throw new RuntimeException("Payment creation failed", e);
        }

        return new HttpResponse(HttpStatus.OK, "Payment created successfully. Payment ID: " + intent.getId());
    }



    private void sendEmail(String paymentId, String email) {
        MimeMessage mimeMessage = javaMailSender.createMimeMessage();
        MimeMessageHelper helper;
        try {
            helper = new MimeMessageHelper(mimeMessage, true);

            String htmlMsg = "<div class=\"content\">" +
                             "<p>Платеж успешно создан, чтобы подтвердить платёж, перейдите по ссылке:</p>" +
                             "<a href=\"http://localhost:8080/api/payment/confirm?paymentId=" +
                             URLEncoder.encode(paymentId, StandardCharsets.UTF_8) +
                             "\" class=\"button\">Подтвердить платёж</a>" +
                             "</div>";

            helper.setText(htmlMsg, true);
            helper.setTo(email);
            helper.setSubject("Подтверждение платежа!");
            helper.setFrom("GADGETARIUM <gadgetarium22@gmail.com>");
            javaMailSender.send(mimeMessage);
        } catch (MessagingException e) {
            throw new BadRequestException("Failed to send email: " + e.getMessage());
        }
    }




    @Override
    public HttpResponse confirmPayment(String paymentId) {
        if (paymentId == null || paymentId.isEmpty()) {
            throw new BadRequestException("Payment ID cannot be null or empty");
        }

        try {
            PaymentIntent paymentIntent = PaymentIntent.retrieve(paymentId);
            PaymentIntent updatedPaymentIntent = paymentIntent.confirm();

            if ("succeeded".equals(updatedPaymentIntent.getStatus())) {
                return new HttpResponse(HttpStatus.OK, "Payment confirmed successfully.");
            } else {
                throw new BadRequestException("Payment confirmation failed. Status: " + updatedPaymentIntent.getStatus());
            }
        } catch (StripeException e) {
            throw new BadRequestException("Payment confirmation failed: " + e.getMessage());
        }
    }



    @Override
    public Long getNew() {
        return orderRepo.getOrderByStatus();
    }
}