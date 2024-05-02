package gadgetarium.dto.response;

import lombok.Builder;

import java.math.BigDecimal;

@Builder
public record ViewedProductsResponse(
        Long id,
        int discount,
        String image,
        String nameOfGadget,
        double rating,
        int countOfFeedback,
        BigDecimal price,
        BigDecimal currentPrice
) {
}
