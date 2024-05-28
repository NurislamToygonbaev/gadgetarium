package gadgetarium.configs.paypal;

import com.fasterxml.jackson.databind.ObjectMapper;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.web.client.RestTemplate;

@Configuration
public class PayPalConfig {

    @Value("${paypal.client-id}")
    private String clientId;

    @Value("${paypal.client-secret}")
    private String clientSecret;

    @Value("${paypal.base-url}")
    private String baseUrl;

    @Bean
    public RestTemplate restTemplate() {
        return new RestTemplate();
    }

    @Bean
    public ObjectMapper objectMapper(){
        return new ObjectMapper();
    }

    @Bean
    public String getClientId() {
        return clientId;
    }

    @Bean
    public String getClientSecret() {
        return clientSecret;
    }

    @Bean
    public String getBaseUrl() {
        return baseUrl;
    }
}