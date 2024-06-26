package gadgetarium.dto.request;

import lombok.Builder;

import java.math.BigDecimal;

@Builder
public record SetPriceAndQuantityRequest(
        Long id,
        BigDecimal price,
        int quantity
) {
}
