package gadgetarium.dto.request;

import gadgetarium.validation.price.PriceValidation;
import lombok.Builder;

import java.math.BigDecimal;

@Builder
public record ProductPriceRequest(
        Long id,
        int quantity,
        @PriceValidation
        BigDecimal price
) {
}
