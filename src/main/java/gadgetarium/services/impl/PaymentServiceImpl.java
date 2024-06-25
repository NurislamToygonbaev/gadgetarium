package gadgetarium.services.impl;

import com.paypal.api.payments.*;
import com.paypal.base.rest.APIContext;
import com.paypal.base.rest.PayPalRESTException;
import gadgetarium.dto.response.HttpResponse;
import gadgetarium.dto.response.OrderImageResponse;
import gadgetarium.dto.response.OrderSuccessResponse;
import gadgetarium.entities.Order;
import gadgetarium.enums.Payment;
import gadgetarium.enums.Status;
import gadgetarium.repositories.OrderRepository;
import gadgetarium.services.PaymentService;
import jakarta.transaction.Transactional;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;

import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.List;
import java.util.Locale;

@Service
@RequiredArgsConstructor
public class PaymentServiceImpl implements PaymentService {

    private final OrderRepository orderRepo;
    private final APIContext apiContext;

    @Override
    public com.paypal.api.payments.Payment createPayment(
            BigDecimal total,
            String currency,
            String method,
            String intent,
            String description,
            String cancelUrl,
            String successUrl
            ) throws PayPalRESTException {
        Amount amount = new Amount();
        amount.setCurrency(currency);
        amount.setTotal(String.format(Locale.caseFoldLanguageTag(currency), "%.2f", total));

        Transaction transaction = new Transaction();
        transaction.setDescription(description);
        transaction.setAmount(amount);

        List<Transaction> transactions = new ArrayList<>();
        transactions.add(transaction);

        Payer payer = new Payer();
        payer.setPaymentMethod(method);

        com.paypal.api.payments.Payment payment = new com.paypal.api.payments.Payment();
        payment.setIntent(intent);
        payment.setPayer(payer);
        payment.setTransactions(transactions);

        RedirectUrls redirectUrls = new RedirectUrls();
        redirectUrls.setCancelUrl(cancelUrl);
        redirectUrls.setReturnUrl(successUrl);

        payment.setRedirectUrls(redirectUrls);

        return payment.create(apiContext);
    }

    @Override
    public com.paypal.api.payments.Payment executePayment(String paymentId, String payerId) throws PayPalRESTException {
        com.paypal.api.payments.Payment payment = new com.paypal.api.payments.Payment();
        payment.setId(paymentId);

        PaymentExecution paymentExecution = new PaymentExecution();
        paymentExecution.setPayerId(payerId);

        return payment.execute(apiContext, paymentExecution);
    }

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
}