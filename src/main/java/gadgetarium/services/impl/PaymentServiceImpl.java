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
import org.springframework.util.LinkedMultiValueMap;
import org.springframework.util.MultiValueMap;
import org.springframework.web.client.RestTemplate;

import java.util.*;

@Service
@RequiredArgsConstructor
public class PaymentServiceImpl implements PaymentService {

    private final OrderRepository orderRepo;
    private final RestTemplate restTemplate;
    private final ObjectMapper objectMapper;
    private final PayPalConfig payPalConfig;

    private String getAccessToken() throws Exception {
        // Encode client ID and secret
        String auth = payPalConfig.getClientId() + ":" + payPalConfig.getClientSecret();
        byte[] encodedAuth = Base64.getEncoder().encode(auth.getBytes());
        String authHeader = "Basic " + new String(encodedAuth);

        // Set headers
        HttpHeaders headers = new HttpHeaders();
        headers.set("Authorization", authHeader);
        headers.setContentType(MediaType.APPLICATION_FORM_URLENCODED);
        headers.setAccept(List.of(MediaType.APPLICATION_JSON)); // Accept header

        // Create the request entity
        HttpEntity<String> entity = new HttpEntity<>("grant_type=client_credentials", headers);

        // Send the request
        ResponseEntity<String> response = restTemplate.exchange(
                payPalConfig.getBaseUrl() + "/v1/oauth2/token",
                HttpMethod.POST,
                entity,
                String.class
        );

        // Check the response status and parse the response body
        if (response.getStatusCode() == HttpStatus.OK) {
            JsonNode jsonNode = objectMapper.readTree(response.getBody());
            return jsonNode.get("access_token").asText();
        }

        // Throw an exception if the token cannot be obtained
        throw new Exception("Failed to obtain access token: " + response.getStatusCode());
    }

    @Override
    public JsonNode createPayment(Long orderId, String currency, String method, String intent, String description, String cancelUrl, String successUrl) throws Exception {
        String accessToken = getAccessToken();
        Order order = orderRepo.getOrderById(orderId);

        HttpHeaders headers = new HttpHeaders();
        headers.set("Authorization", "Bearer " + accessToken);
        headers.setContentType(MediaType.APPLICATION_JSON);
        headers.setAccept(List.of(MediaType.APPLICATION_JSON)); // Accept header

        // Prepare the amount details
        Map<String, Object> amountDetails = new HashMap<>();
        amountDetails.put("total", String.format("%.2f", order.getTotalPrice()));
        amountDetails.put("currency", currency);

        // Prepare the amount object
        Map<String, Object> amount = new HashMap<>();
        amount.put("total", String.format("%.2f", order.getTotalPrice())); // For the correct PayPal API use the 'total' key directly
        amount.put("currency", currency);

        // Prepare the transaction object
        Map<String, Object> transaction = new HashMap<>();
        transaction.put("amount", amount);
        transaction.put("description", description);

        // Prepare the payer object
        Map<String, Object> payer = new HashMap<>();
        payer.put("payment_method", method);

        // Prepare the redirect URLs
        Map<String, Object> redirectUrls = new HashMap<>();
        redirectUrls.put("cancel_url", cancelUrl);
        redirectUrls.put("return_url", successUrl);

        // Prepare the payment details
        Map<String, Object> paymentDetails = new HashMap<>();
        paymentDetails.put("intent", intent);
        paymentDetails.put("payer", payer);
        paymentDetails.put("transactions", List.of(transaction));
        paymentDetails.put("redirect_urls", redirectUrls);

        // Create the request entity
        HttpEntity<Map<String, Object>> entity = new HttpEntity<>(paymentDetails, headers);

        // Send the request
        ResponseEntity<String> response = restTemplate.postForEntity(payPalConfig.getBaseUrl() + "/v1/payments/payment", entity, String.class);

        // Check the response status and parse the response body
        if (response.getStatusCode() == HttpStatus.CREATED) {
            return objectMapper.readTree(response.getBody());
        }

        // Throw an exception if the payment cannot be created
        throw new Exception("Failed to create payment: " + response.getStatusCode());
    }

    @Override
    public JsonNode executePayment(String paymentId, String payerId) throws Exception {
        String accessToken = getAccessToken();

        HttpHeaders headers = new HttpHeaders();
        headers.set("Authorization", "Bearer " + accessToken);
        headers.setContentType(MediaType.APPLICATION_JSON);
        headers.setAccept(List.of(MediaType.APPLICATION_JSON)); // Accept header

        // Prepare the execute payment details
        Map<String, Object> paymentExecute = new HashMap<>();
        paymentExecute.put("payer_id", payerId);

        // Create the request entity
        HttpEntity<Map<String, Object>> entity = new HttpEntity<>(paymentExecute, headers);

        // Send the request
        ResponseEntity<String> response = restTemplate.postForEntity(
                payPalConfig.getBaseUrl() + "/v1/payments/payment/" + paymentId + "/execute",
                entity,
                String.class
        );

        // Check the response status and parse the response body
        if (response.getStatusCode() == HttpStatus.OK) {
            return objectMapper.readTree(response.getBody());
        }

        // Throw an exception if the payment cannot be executed
        throw new Exception("Failed to execute payment: " + response.getStatusCode());
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