package gadgetarium.dto.request;

import jakarta.validation.constraints.Size;
import lombok.Builder;

import java.util.List;

@Builder
public record BasketIdsRequest(
        @Size(min = 0)
        List<Long> ids
) {
}
