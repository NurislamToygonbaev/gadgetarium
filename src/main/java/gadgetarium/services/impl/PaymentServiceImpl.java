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
import gadgetarium.services.PaymentService;
import jakarta.transaction.Transactional;
import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.HttpStatus;
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
    public HttpResponse createPayment(Long orderId, String token) {
        User user = currentUser.get();
        Order order = orderRepo.getOrderById(orderId);

        CompletableFuture<Void> future = CompletableFuture.runAsync(() ->
                createPaymentIntent(user, token, Long.valueOf(String.valueOf(order.getTotalPrice()))));
        future.join();
        return HttpResponse.builder()
                .status(HttpStatus.OK)
                .message("")
                .build();
    }

    private void createPaymentIntent(User user, String token, Long amount){
        Stripe.apiKey = stripeKey;

        try {
            CustomerSearchParams params = CustomerSearchParams.builder()
                    .setQuery("email:\"" + user.getEmail() + "\"")
                    .build();
            CustomerSearchResult customers = Customer.search(params);

            List<Customer> customersData = customers.getData();

            PaymentMethodCreateParams paymentMethodCreateParams = PaymentMethodCreateParams
                    .builder()
                    .setType(PaymentMethodCreateParams.Type.CARD)
                    .setBillingDetails(PaymentMethodCreateParams.BillingDetails.builder()
                            .setEmail(customersData.getFirst().getEmail())
                            .setName(customersData.getFirst().getName())
                            .build())
                    .setCard(PaymentMethodCreateParams.Token.builder()
                            .setToken(token)
                            .build())
                    .build();

            PaymentMethod paymentMethod = PaymentMethod.create(paymentMethodCreateParams);

            PaymentMethodAttachParams attachParams = PaymentMethodAttachParams
                    .builder()
                    .setCustomer(customersData.getFirst().getId())
                    .build();
            paymentMethod = paymentMethod.attach(attachParams);

            PaymentIntentCreateParams paymentIntentCreateParams = PaymentIntentCreateParams.builder()
                    .setAmount(amount * 100)
                    .setPaymentMethod(paymentMethod.getId())
                    .setCustomer(customersData.getFirst().getId())
                    .setAutomaticPaymentMethods(PaymentIntentCreateParams.AutomaticPaymentMethods
                            .builder()
                            .setAllowRedirects(PaymentIntentCreateParams.AutomaticPaymentMethods.AllowRedirects.NEVER)
                            .setEnabled(true)
                            .build())
                    .setCurrency("kgs")
                    .build();

            PaymentIntent intent = PaymentIntent.create(paymentIntentCreateParams);

        }catch (StripeException e){
            throw new BadRequestException(e.getMessage());
        }catch (NoSuchElementException e){
            throw new NotFoundException("not found");
        }
    }

    @Override
    public HttpResponse confirmPayment(String paymentId) {
        return null;
    }
}