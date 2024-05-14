package gadgetarium.dto.response;

import gadgetarium.enums.Payment;
import lombok.Builder;

import java.math.BigDecimal;

@Builder
public record OrderOverViewResponse(
        BigDecimal price,
        String delivery,
        Payment payment
) {
}
