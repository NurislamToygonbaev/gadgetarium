package gadgetarium.dto.response;

import lombok.Builder;

import java.time.LocalDate;

@Builder
public record OrderNumber(
        Long number,
        LocalDate date,
        String email
) {
}
