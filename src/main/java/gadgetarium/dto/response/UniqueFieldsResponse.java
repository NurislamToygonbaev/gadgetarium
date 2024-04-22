package gadgetarium.dto.response;

import java.util.List;
import java.util.Map;

public record UniqueFieldsResponse(List<String> uniqueFields, Map<String, String> uniqueCharacteristics)implements SampleResponse {
}

