package gadgetarium.services.impl;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import gadgetarium.configs.paypal.PayPalConfig;
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
import org.springframework.http.*;
import org.springframework.stereotype.Service;
import org.springframework.web.client.RestTemplate;

import java.util.Base64;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

@Service
@RequiredArgsConstructor
public class PaymentServiceImpl implements PaymentService {

    private final OrderRepository orderRepo;
    private final RestTemplate restTemplate;
    private final ObjectMapper objectMapper;
    private final PayPalConfig payPalConfig;

    private String getAccessToken() throws Exception {
        String auth = payPalConfig.getClientId() + ":" + payPalConfig.getClientSecret();
        byte[] encodedAuth = Base64.getEncoder().encode(auth.getBytes());
        String authHeader = "Basic " + new String(encodedAuth);

        HttpHeaders headers = new HttpHeaders();
        headers.set("Authorization", authHeader);
        headers.setContentType(MediaType.APPLICATION_FORM_URLENCODED);

        HttpEntity<String> entity = new HttpEntity<>("grant_type=client_credentials", headers);

        ResponseEntity<String> response = restTemplate.exchange(payPalConfig.getBaseUrl() + "/v1/oauth2/token", HttpMethod.POST, entity, String.class);

        if (response.getStatusCode() == HttpStatus.OK) {
            JsonNode jsonNode = objectMapper.readTree(response.getBody());
            return jsonNode.get("access_token").asText();
        }

        throw new Exception("Failed to obtain access token");
    }

    @Override
    public JsonNode createPayment(Long orderId, String currency, String method, String intent, String description, String cancelUrl, String successUrl) throws Exception {
        String accessToken = getAccessToken();
        Order order = orderRepo.getOrderById(orderId);

        HttpHeaders headers = new HttpHeaders();
        headers.set("Authorization", "Bearer " + accessToken);
        headers.setContentType(MediaType.APPLICATION_JSON);

        Map<String, Object> amountDetails = new HashMap<>();
        amountDetails.put("total", String.format("%.2f", order.getTotalPrice()));
        amountDetails.put("currency", currency);

        Map<String, Object> amount = new HashMap<>();
        amount.put("amount", amountDetails);

        Map<String, Object> transaction = new HashMap<>();
        transaction.put("amount", amount);
        transaction.put("description", description);

        Map<String, Object> payer = new HashMap<>();
        payer.put("payment_method", method);

        Map<String, Object> redirectUrls = new HashMap<>();
        redirectUrls.put("cancel_url", cancelUrl);
        redirectUrls.put("return_url", successUrl);

        Map<String, Object> paymentDetails = new HashMap<>();
        paymentDetails.put("intent", intent);
        paymentDetails.put("payer", payer);
        paymentDetails.put("transactions", List.of(transaction));
        paymentDetails.put("redirect_urls", redirectUrls);

        HttpEntity<Map<String, Object>> entity = new HttpEntity<>(paymentDetails, headers);

        ResponseEntity<String> response = restTemplate.postForEntity(payPalConfig.getBaseUrl() + "/v1/payments/payment", entity, String.class);

        if (response.getStatusCode() == HttpStatus.CREATED) {
            return objectMapper.readTree(response.getBody());
        }

        throw new Exception("Failed to create payment");
    }

    @Override
    public JsonNode executePayment(String paymentId, String payerId) throws Exception {
        String accessToken = getAccessToken();

        HttpHeaders headers = new HttpHeaders();
        headers.set("Authorization", "Bearer " + accessToken);
        headers.setContentType(MediaType.APPLICATION_JSON);

        Map<String, Object> paymentExecute = new HashMap<>();
        paymentExecute.put("payer_id", payerId);

        HttpEntity<Map<String, Object>> entity = new HttpEntity<>(paymentExecute, headers);

        ResponseEntity<String> response = restTemplate.postForEntity(payPalConfig.getBaseUrl() + "/v1/payments/payment/" + paymentId + "/execute", entity, String.class);

        if (response.getStatusCode() == HttpStatus.OK) {
            return objectMapper.readTree(response.getBody());
        }

        throw new Exception("Failed to execute payment");
    }

    @Override
    @Transactional
    public HttpResponse typeOrder(Long orderId, Payment payment) {
        Order order = orderRepo.getOrderById(orderId);
        order.setPayment(payment);
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
        return OrderSuccessResponse.builder()
                .number(order.getNumber())
                .createAd(String.valueOf(order.getCreatedAt()))
                .email(order.getUser().getEmail())
                .build();
    }
}