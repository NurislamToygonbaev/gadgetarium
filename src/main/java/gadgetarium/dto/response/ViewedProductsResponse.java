package gadgetarium.dto.response;

import lombok.Builder;

import java.math.BigDecimal;

@Builder
public record ViewedProductsResponse(
        Long gadgetId,
        Long subGadgetId,
        int discount,
        String image,
        String nameOfGadget,
        double rating,
        int countOfFeedback,
        BigDecimal price,
        BigDecimal currentPrice
) {
}
