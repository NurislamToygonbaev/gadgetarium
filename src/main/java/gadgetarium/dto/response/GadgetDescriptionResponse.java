package gadgetarium.dto.response;

import lombok.Builder;

@Builder
public record GadgetDescriptionResponse(
        Long id,
        String videoUrl,
        String description
){}
