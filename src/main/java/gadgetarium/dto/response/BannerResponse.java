package gadgetarium.dto.response;

import gadgetarium.entities.Banner;
import lombok.Builder;

import java.util.List;

@Builder
public record BannerResponse(
        Long id,
        List<String> images
) {
}
