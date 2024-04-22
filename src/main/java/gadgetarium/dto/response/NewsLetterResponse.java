package gadgetarium.dto.response;

import lombok.Builder;

@Builder
public record NewsLetterResponse(
        String message
) {
}
