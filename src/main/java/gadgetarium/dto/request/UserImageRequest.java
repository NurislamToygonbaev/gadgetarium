package gadgetarium.dto.request;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import lombok.Builder;

@Builder
public record UserImageRequest(
        @NotNull
        String image
) {
}
