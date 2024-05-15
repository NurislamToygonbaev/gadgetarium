package gadgetarium.dto.response;

import lombok.Builder;

@Builder
public record CatResponse(
        Long id,
        String categoryName
) {
}
