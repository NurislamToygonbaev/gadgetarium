package gadgetarium.dto.request;

import gadgetarium.validation.banner.BannerValidation;
import lombok.Builder;
import java.util.List;

@Builder
public record BannerRequest(
        @BannerValidation
        List<String> images
) {
}
