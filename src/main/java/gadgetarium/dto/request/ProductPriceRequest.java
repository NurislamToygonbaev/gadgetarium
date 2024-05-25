package gadgetarium.dto.request;

import gadgetarium.validation.price.PriceValidation;
import gadgetarium.validation.quantity.QuantityValidation;
import lombok.Builder;

import java.math.BigDecimal;

@Builder
public record ProductPriceRequest(
        @PriceValidation
        BigDecimal price
) {
}
