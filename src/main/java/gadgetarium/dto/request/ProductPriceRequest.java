package gadgetarium.dto.request;

import lombok.Builder;

import java.math.BigDecimal;

@Builder
public record ProductPriceRequest(
        Long id,
        int quantity,
        BigDecimal price
) {
}
