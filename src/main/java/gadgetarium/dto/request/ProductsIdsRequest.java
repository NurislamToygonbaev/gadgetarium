package gadgetarium.dto.request;

import lombok.Builder;

import java.math.BigDecimal;
import java.util.List;

@Builder
public record ProductsIdsRequest(
        List<Long> ids,
        BigDecimal price,
        int quantity
) {
}
