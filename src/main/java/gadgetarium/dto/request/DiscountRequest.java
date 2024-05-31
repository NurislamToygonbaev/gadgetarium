package gadgetarium.dto.request;

import jakarta.validation.constraints.NotNull;
import lombok.Builder;

import java.time.LocalDate;
import java.util.List;

@Builder
public record DiscountRequest(
        @NotNull
        List<Long> gadgetId,
        @NotNull(message = "Discount size day cannot be null")
        int discountSize,
        @NotNull(message = "Start day cannot be null")
        LocalDate startDay,
        @NotNull(message = "End day cannot be null")
        LocalDate endDay
){
}
