package gadgetarium.dto.response;

import gadgetarium.enums.Memory;
import lombok.Builder;

import java.math.BigDecimal;

@Builder
public record GadgetsResponse(
         Long id,
         String image,
         int quantity,
         String brandNameOfGadget,
         String memory,
         String colour,
         int rating,
         int countOfRating,
         BigDecimal price,
         BigDecimal currentPrice,
         int percent,
         boolean likes,
         boolean compression,
         boolean basked
) {
}
