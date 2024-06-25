package gadgetarium.dto.request;

import gadgetarium.enums.Memory;
import gadgetarium.enums.Ram;
import gadgetarium.validations.countSim.CountSimValidation;
import gadgetarium.validations.feedack.ImageValidation;
import gadgetarium.validations.price.PriceValidation;
import jakarta.validation.constraints.Min;
import lombok.Builder;

import java.math.BigDecimal;
import java.util.List;

@Builder
public record GadgetNewDataRequest(
        @Min(value = 0)
        int quantity,
        @PriceValidation
        BigDecimal price,
        String colour,
        @CountSimValidation
        int countSim,
        Memory memory,
        Ram ram,
        String materialBracelet,
        String materialBody,
        String sizeWatch,
        String dumas,
        String genderWatch,
        String waterproof,
        String wireless,
        String shapeBody

) {
}
