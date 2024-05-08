package gadgetarium.dto.response;

import lombok.Builder;

import java.time.LocalDate;

@Builder
public record StripeResponse(
        Long number,
        LocalDate localDate,
        String email,
        String message
) {

}
