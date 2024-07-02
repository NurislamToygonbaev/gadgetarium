package gadgetarium.dto.request;

import gadgetarium.validations.quantity.QuantityValidation;
import jakarta.validation.constraints.NotNull;
import lombok.Builder;

import java.util.List;

@Builder
public record IdsGadgetAndQuantityRequest(
        List<IdsAndQuantity> idsAndQuantities
) {
}
