package gadgetarium.dto.response;

import lombok.Builder;

import java.math.BigDecimal;

@Builder
public record InfoResponse (
     BigDecimal buyPrice,
     int buyCount,
     BigDecimal orderPrice,
     int orderCount
){
}
