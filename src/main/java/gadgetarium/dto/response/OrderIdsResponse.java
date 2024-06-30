package gadgetarium.dto.response;

import lombok.Builder;

@Builder
public record OrderIdsResponse(
        Long orderId
) {
}
