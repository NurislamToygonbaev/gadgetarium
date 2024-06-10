package gadgetarium.dto.response;

import lombok.Builder;

import java.util.List;
import java.util.Map;

@Builder
public record UniqueFieldResponse(
        CompareFieldResponse compareFieldResponse,
        List<Map<String, String>> uniqueCharacteristics
)implements SampleResponse{}
