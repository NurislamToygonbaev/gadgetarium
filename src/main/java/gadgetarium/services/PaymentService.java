package gadgetarium.services;

import com.fasterxml.jackson.databind.JsonNode;

public interface PaymentService {

    JsonNode createPayment(Long id, String currency, String paypal, String sale, String paymentDescription, String url, String url1) throws Exception;

    JsonNode executePayment(String s, String s1) throws Exception;
}