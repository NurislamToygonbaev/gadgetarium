package gadgetarium.dto.request;

import lombok.Builder;

@Builder
public record PaymentRequest(
        Long id,
        String currency
) {
}