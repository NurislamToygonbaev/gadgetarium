package gadgetarium.dto.response;

import lombok.Builder;

import java.math.BigDecimal;

@Builder
public record GetAllBasketResponse(
        Long gadgetId,
        Long subGadgetId,
        String image,
        String nameOfGadget,
        String memory,
        String colour,
        double rating,
        int countOfRating,
        int quantity,
        Long article,
        BigDecimal price,
        int countOfGadget,
        boolean likes
) {
}
