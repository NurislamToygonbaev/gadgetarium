package gadgetarium.dto.response;

import lombok.Builder;

@Builder
public record PaymentIdResponse(
        String paymentId,
        HttpResponse httpResponse
) {
}
