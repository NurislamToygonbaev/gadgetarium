package gadgetarium.dto.request;

import gadgetarium.enums.Memory;
import gadgetarium.enums.Ram;
import lombok.Builder;

import java.util.List;

@Builder
public record ProductsRequest(
        String mainColour,
        Memory memory,
        Ram ram,
        int countSim,
        List<String> images
) {
}
