package gadgetarium.dto.response;

import lombok.Builder;

@Builder
public record S3Response(
        Object data,
        HttpResponse httpResponse
) {
}
