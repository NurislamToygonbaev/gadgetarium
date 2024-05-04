package gadgetarium.dto.response;

import lombok.Builder;

import java.math.BigDecimal;

@Builder
public record GetBasketAmounts(
        int quantity,
        BigDecimal discountPrice,
        BigDecimal price,
        BigDecimal currentPrice
) {
}
