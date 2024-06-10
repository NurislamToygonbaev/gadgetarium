package gadgetarium.dto.response;

import gadgetarium.enums.Memory;
import lombok.Builder;

import java.math.BigDecimal;
import java.util.List;
import java.util.Map;
@Builder
public record WatchRespo(
        Long id,
        String image,
        String nameOfGadget,
        BigDecimal price,
        String mainColour,
        String brandName,
        Memory memory,
        List<String> uniqF
)implements SampleResponse {
}
