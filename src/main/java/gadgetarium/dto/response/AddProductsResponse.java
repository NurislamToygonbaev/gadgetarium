package gadgetarium.dto.response;

import gadgetarium.enums.Memory;
import gadgetarium.enums.Ram;
import lombok.Builder;

import java.time.LocalDate;

@Builder
public record AddProductsResponse(
        Long id,
        String brandName,
        String mainColour,
        String memory,
        String ram,
        int countSim,
        LocalDate releaseDate
) {
}
