package gadgetarium.dto.response;

import lombok.Builder;

import java.math.BigDecimal;
import java.util.List;

@Builder
public record OrderResponseFindById(
        Long id,
        String fullName,
        Long number,
        List<String> nameOfGadget,
        List<String> memory,
        List<String> colour,
        int count,
        BigDecimal price,
        List<Integer> percent,
        BigDecimal discountPrice,
        BigDecimal totalPrice
) {
}
