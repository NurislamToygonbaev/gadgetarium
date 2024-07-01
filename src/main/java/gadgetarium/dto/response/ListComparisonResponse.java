package gadgetarium.dto.response;

import gadgetarium.enums.Memory;
import lombok.Builder;

import java.math.BigDecimal;
import java.util.List;

@Builder
public record ListComparisonResponse(
        Long id,
        List<String> images,
        String nameOfGadget,
        String mainColor,
        String memory,
        BigDecimal price,
        boolean basket

){}
