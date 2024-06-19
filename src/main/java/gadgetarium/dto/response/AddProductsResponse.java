package gadgetarium.dto.response;


import lombok.Builder;

import java.math.BigDecimal;
import java.time.LocalDate;

@Builder
public record AddProductsResponse(
        Long gadgetId,
        Long subGadgetId,
        String brandName,
        String mainColour,
        String memory,
        String ram,
        int countSim,
        LocalDate releaseDate,
        int quantity,
        BigDecimal price
) {
}
