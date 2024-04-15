package gadgetarium.dto.response;

import lombok.Builder;

import java.math.BigDecimal;
import java.util.List;
import java.util.Map;

@Builder
public record GadgetResponse(
        String brandLogo,
        List<String> images,
        String nameOfGadget,
        int quantity,
        Long articleNumber,
        int rating,
        List<String> mainColour,
        Map<String, String> characteristics,
        BigDecimal price
        ) {
}