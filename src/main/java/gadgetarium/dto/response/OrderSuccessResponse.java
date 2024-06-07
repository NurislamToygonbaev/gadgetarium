package gadgetarium.dto.response;

import lombok.Builder;

@Builder
public record OrderSuccessResponse(
        Long number,
        String createAd,
        String email
) {
}
