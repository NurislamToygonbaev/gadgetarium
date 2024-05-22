package gadgetarium.dto.response;

import lombok.Builder;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.List;

@Builder
public record PaginationGadget(
        Long id,
        String images,
        Long article,
        String nameOfGadget,
        LocalDate releaseDate,
        int quantity,
        BigDecimal price,
        int percent,
        BigDecimal currentPrice
) {
}
