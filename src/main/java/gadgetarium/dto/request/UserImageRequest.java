package gadgetarium.dto.request;

import jakarta.validation.constraints.NotBlank;
import lombok.Builder;

@Builder
public record UserImageRequest(
        @NotBlank
        String image
) {
}
