package gadgetarium.dto.response;

import lombok.Builder;

import java.util.List;
import java.util.Map;

@Builder
public record Responsee(
        List<Map<String, String>> characteristic
) {

}
