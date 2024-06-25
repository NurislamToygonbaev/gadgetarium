package gadgetarium.dto.response;

import lombok.Builder;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.List;

@Builder
public record PaginationGadget(
        Long gadgetId,
        Long subGadgetId,
        String images,
        Long article,
        String nameOfGadget,
        String createdAt,
        int quantity,
        BigDecimal price,
        int percent,
        BigDecimal currentPrice
) {
}
