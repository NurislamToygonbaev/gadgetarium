package gadgetarium.dto.response;

import lombok.Builder;

import java.math.BigDecimal;

@Builder
public record DetailsResponse(
        Long gadgetId,
        Long subGadgetId,
        String image,
        String nameOfGadget,
        String colour,
        int countSim,
        String ram,
        String memory,
        int quantity,
        BigDecimal price
) {
}
