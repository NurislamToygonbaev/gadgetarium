package gadgetarium.dto.response;

import gadgetarium.enums.Status;
import lombok.Builder;

import java.math.BigDecimal;
import java.time.LocalDate;

@Builder
public record AllOrderHistoryResponse(
        Long id,
        LocalDate createdAt,
        Long number,
        Status status,
        BigDecimal deliveryPrice


){}