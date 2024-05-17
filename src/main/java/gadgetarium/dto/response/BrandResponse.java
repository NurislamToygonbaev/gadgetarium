package gadgetarium.dto.response;

import lombok.Builder;

@Builder
public record BrandResponse(
        Long id,
        String image,
        String brandName
) {
}
