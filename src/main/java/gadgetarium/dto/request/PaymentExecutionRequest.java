package gadgetarium.dto.request;

import lombok.Builder;

@Builder
public record PaymentExecutionRequest(
        String paymentId,
        String payerId
) {
}