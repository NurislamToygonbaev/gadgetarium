package gadgetarium.dto.response;

import lombok.Builder;

import java.util.List;

@Builder
public record AddGadgetResponse(
        List<Long> ids,
        HttpResponse httpResponse
) {
}
