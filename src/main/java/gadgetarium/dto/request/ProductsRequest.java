package gadgetarium.dto.request;

import gadgetarium.enums.Memory;
import gadgetarium.enums.Ram;
import gadgetarium.validation.countSim.CountSimValidation;
import lombok.Builder;

import java.util.List;

@Builder
public record ProductsRequest(
        String mainColour,
        Memory memory,
        Ram ram,
        @CountSimValidation
        int countSim,
        List<String> images
) {
}
