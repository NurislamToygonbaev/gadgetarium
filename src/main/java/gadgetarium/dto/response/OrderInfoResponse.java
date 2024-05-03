package gadgetarium.dto.response;

import lombok.Builder;

@Builder
public record OrderInfoResponse(
        Long id,
        Long number,
        String status,
        String phoneNumber,
        String address
) {
}
