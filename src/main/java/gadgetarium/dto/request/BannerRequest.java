package gadgetarium.dto.request;

import gadgetarium.validations.banner.BannerValidation;
import lombok.Builder;
import java.util.List;

@Builder
public record BannerRequest(
        @BannerValidation
        List<String> images
) {
}
