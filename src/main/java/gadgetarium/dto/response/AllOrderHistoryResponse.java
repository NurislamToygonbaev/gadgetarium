package gadgetarium.dto.response;

import com.amazonaws.services.s3.internal.S3RequestEndpointResolver;
import gadgetarium.enums.Status;
import lombok.Builder;

import java.math.BigDecimal;
import java.time.LocalDate;

@Builder
public record AllOrderHistoryResponse(
        Long id,
        String createdAt,
        Long number,
        String status,
        BigDecimal deliveryPrice


){}
