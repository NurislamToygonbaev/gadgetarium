package gadgetarium.dto.response;

import gadgetarium.enums.Memory;
import lombok.Builder;

import java.math.BigDecimal;
import java.util.List;
import java.util.Map;

@Builder
public record SubGadgetResponse(
        Long id,
        String nameOfGadget,
        BigDecimal price,
        String mainColour,
        String brandName,
        Memory memory,
        Map<String, String> characteristics,
        List<String> differentFields
){}
