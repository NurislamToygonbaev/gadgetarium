package gadgetarium.dto.response;

import gadgetarium.enums.Status;
import lombok.Builder;

import java.math.BigDecimal;

@Builder
public record OrderResponseFindById(
        Long id,
        String fullName,
        Long number,
        String nameOfGadget,
        String memory,
        String colour,
        int count,
        BigDecimal price,
        int percent,
        BigDecimal discountPrice,
        BigDecimal totalPrice
) {
}
