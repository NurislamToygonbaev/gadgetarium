package gadgetarium.dto.request;

import gadgetarium.validations.quantity.QuantityValidation;
import jakarta.validation.constraints.NotNull;
import lombok.Builder;

@Builder
public record IdsGadgetAndQuantityRequest(
        @NotNull
        Long id,
        @QuantityValidation
        int quantity
) {
}
