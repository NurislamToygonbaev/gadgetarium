package gadgetarium.dto.request;

import gadgetarium.enums.Memory;
import gadgetarium.enums.Ram;
import gadgetarium.validation.countSim.CountSimValidation;
import lombok.Builder;

import java.util.List;

@Builder
public record ProductsRequest(
        String mainColour,
        String memory,
        String ram,
        @CountSimValidation
        int countSim,
        List<String> images
) {
}
