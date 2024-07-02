package gadgetarium.dto.request;

import gadgetarium.validations.quantity.QuantityValidation;
import lombok.Builder;

@Builder
public record IdsAndQuantity(
        Long id,
        @QuantityValidation
        int quantity
) {
}
