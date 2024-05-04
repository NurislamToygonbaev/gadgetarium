package gadgetarium.dto.response;

import lombok.Builder;

@Builder
public record GadgetDescriptionResponse(
        String videoUrl,
        String description
){}
