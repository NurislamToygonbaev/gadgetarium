package gadgetarium.dto.request;

import lombok.Builder;

import java.util.List;

@Builder
public record ProductDocRequest(
        String pdf,
        String videoUrl,
        String description
) {
}
