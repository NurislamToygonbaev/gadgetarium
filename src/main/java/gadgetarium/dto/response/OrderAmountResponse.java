package gadgetarium.dto.response;

import lombok.Builder;

import java.math.BigDecimal;

@Builder
public record OrderAmountResponse(
        int quantity,
        BigDecimal discountPrice,
        BigDecimal price,
        BigDecimal currentPrice
) {
}
