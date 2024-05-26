package gadgetarium.dto.response;

import gadgetarium.enums.Memory;
import lombok.Builder;

import java.math.BigDecimal;

@Builder
public record GadgetResponseMainPage(
        Long gadgetId,
        Long subGadgetId,
        int percent,
        String image,
        int quantity,
        String nameOfGadget,
        String memory,
        String colour,
        double rating,
        int count,
        BigDecimal price,
        BigDecimal currentPrice,
        boolean likes,
        boolean comparison,
        boolean basket
) {
}
