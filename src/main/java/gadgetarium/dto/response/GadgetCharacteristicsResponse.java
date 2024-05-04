package gadgetarium.dto.response;

import gadgetarium.entities.CharValue;
import lombok.Builder;

import java.util.Map;

@Builder
public record GadgetCharacteristicsResponse(
        Map<String, Map<String, String>> mainCharacteristics
//        Map<String, String> memoryAndProcessor
){}
