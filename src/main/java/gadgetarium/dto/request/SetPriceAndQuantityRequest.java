package gadgetarium.dto.request;

import gadgetarium.validations.price.PriceValidation;
import gadgetarium.validations.quantity.QuantityValidation;
import lombok.Builder;

import java.math.BigDecimal;

@Builder
public record SetPriceAndQuantityRequest(
        @QuantityValidation
        Long id,
        @PriceValidation
        BigDecimal price,
        @QuantityValidation
        int quantity
) {
}
