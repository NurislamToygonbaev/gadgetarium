package gadgetarium.dto.request;

import gadgetarium.validations.price.PriceValidation;
import gadgetarium.validations.quantity.QuantityValidation;
import jakarta.validation.constraints.Min;
import lombok.Builder;
import lombok.NonNull;

import java.math.BigDecimal;

@Builder
public record SetPriceAndQuantityRequest(
        @NonNull
        Long id,
        @PriceValidation
        BigDecimal price,
        @Min(1)
        int quantity
) {
}
