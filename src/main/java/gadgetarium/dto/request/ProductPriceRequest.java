package gadgetarium.dto.request;

import gadgetarium.validations.price.PriceValidation;
import lombok.Builder;

import java.math.BigDecimal;

@Builder
public record ProductPriceRequest(
        @PriceValidation
        BigDecimal price
) {
}
