package gadgetarium.dto.response;

import lombok.Builder;

import java.math.BigDecimal;
import java.time.LocalDate;

@Builder
public record OrderResponse(
        Long id,
        String fullName,
        Long article,
        String date,
        int count,
        BigDecimal price,
        boolean typeOrder,
        String status
) {
}
