package gadgetarium.dto.response;

import gadgetarium.entities.CharValue;
import lombok.Builder;

import java.util.Map;

@Builder
public record GadgetCharacteristicsResponse(
        Long id,
        Map<String, Map<String, String>> mainCharacteristics
){}
