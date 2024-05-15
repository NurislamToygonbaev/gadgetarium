package gadgetarium.dto.request;

import gadgetarium.validation.price.PriceValidation;
import lombok.Builder;

import java.math.BigDecimal;
import java.util.List;

@Builder
public record ProductsIdsRequest(
        List<Long> ids,
        @PriceValidation
        BigDecimal price,
        int quantity
) {
}
