package gadgetarium.dto.request;

import lombok.Builder;
import lombok.NonNull;

@Builder
public record GadgetImagesRequest(
        @NonNull
        String oldKey,
        @NonNull
        String oldImage,
        @NonNull
        String newImage
) {
}
