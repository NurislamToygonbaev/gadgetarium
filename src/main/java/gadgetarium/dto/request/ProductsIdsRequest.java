package gadgetarium.dto.request;

import gadgetarium.validation.price.PriceValidation;
import gadgetarium.validation.quantity.QuantityValidation;
import lombok.Builder;

import java.math.BigDecimal;
import java.util.List;

@Builder
public record ProductsIdsRequest(
        @PriceValidation
        BigDecimal price,
        @QuantityValidation
        int quantity
) {
}
