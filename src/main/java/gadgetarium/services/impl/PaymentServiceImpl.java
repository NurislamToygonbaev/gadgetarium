package gadgetarium.services.impl;

import com.stripe.Stripe;
import com.stripe.exception.StripeException;
import com.stripe.model.Customer;
import com.stripe.model.CustomerSearchResult;
import com.stripe.model.PaymentIntent;
import com.stripe.model.PaymentMethod;
import com.stripe.param.CustomerSearchParams;
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
import jakarta.mail.Message;
import jakarta.mail.MessagingException;
import jakarta.mail.Session;
import jakarta.mail.internet.InternetAddress;
import jakarta.mail.internet.MimeMessage;
import jakarta.transaction.Transactional;
import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.HttpStatus;
import org.springframework.mail.javamail.JavaMailSender;
import org.springframework.mail.javamail.MimeMessageHelper;
import org.springframework.stereotype.Service;

import java.math.BigDecimal;
import java.util.List;
import java.util.NoSuchElementException;
import java.util.concurrent.CompletableFuture;

@Service
@RequiredArgsConstructor
public class PaymentServiceImpl implements PaymentService {

    private final OrderRepository orderRepo;
    private final CurrentUser currentUser;
    private final JavaMailSender javaMailSender;
    private final UserRepository userRepo;

    @Value("${stripe.api}")
    private String stripeKey;


    @Override
    @Transactional
    public HttpResponse typeOrder(Long orderId, Payment payment) {
        Order order = orderRepo.getOrderById(orderId);
        order.setPayment(payment);
        orderRepo.save(order);
        return HttpResponse.builder().status(HttpStatus.OK)
                .message("success changed order with ID: "+order.getId()).build();
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
    public HttpResponse createPayment(Long orderId, String token){
        Order order = orderRepo.getOrderById(orderId);
        PaymentIntentCreateParams params = PaymentIntentCreateParams.builder()
                .setAmount(Long.parseLong(String.valueOf(order.getTotalPrice())) * 100L)
                .setCurrency("kgs")
                .addPaymentMethodType("card")
                .setDescription("Order ID: " + orderId)
                .setReceiptEmail(token)
                .build();

        PaymentIntent paymentIntent = null;
        try {
            paymentIntent = PaymentIntent.create(params);
        } catch (StripeException e) {
            throw new RuntimeException("failed");
        }

        sendEmail(token, paymentIntent.getId());

        return new HttpResponse(HttpStatus.OK, "Payment created successfully and confirmation email sent.");
    }

    @Override
    public HttpResponse confirmPayment(String paymentId){
        try {
            PaymentIntent paymentIntent = PaymentIntent.retrieve(paymentId);
            PaymentIntent updatedPaymentIntent = paymentIntent.confirm();

            if ("succeeded".equals(updatedPaymentIntent.getStatus())) {
                return new HttpResponse(HttpStatus.OK, "Payment confirmed successfully.");
            } else {
                throw new BadRequestException("Payment confirmation failed.,");
            }
        }catch (StripeException e){
            throw new BadRequestException("Payment confirmation failed.,");
        }
    }


    private void sendEmail(String email, String paymentId) {
        MimeMessage mimeMessage = javaMailSender.createMimeMessage();
        MimeMessageHelper helper;
        try {
            helper = new MimeMessageHelper(mimeMessage, true);
            String htmlMsg = "<div class=\"content\">" +
                             "                    \"<p>Платеж успешно создан, чтобы подтвердить платёж, перейдите по ссылке:</p>" +
                             "                    \"<a href=\"http://localhost:8080/api/payment/confirm?paymentId=" + paymentId + "\" class=\"button\">Подтвердить платёж</a>" +
                             "                    \"</div>\"";
            helper.setText(htmlMsg, true);
            helper.setTo(email);
            helper.setSubject("Забыли пароль!");
            helper.setFrom("GADGETARIUM");
            javaMailSender.send(mimeMessage);
        } catch (MessagingException e) {
            throw new NotFoundException(e.getMessage());
        }
    }
}