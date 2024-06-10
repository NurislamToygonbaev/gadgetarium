package gadgetarium.dto.response;

import java.util.List;

public record WatchResponse(
        CompareFieldResponse compareFieldResponse,
        List<String> uniFiled
)implements SampleResponse{
}
