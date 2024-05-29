package gadgetarium.dto.response;

import gadgetarium.enums.Memory;
import lombok.Builder;
import lombok.Getter;
import lombok.Setter;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.List;
import java.util.Map;

@Builder
public record GadgetResponse(
        Long gadgetId,
        Long subGadgetId,
        String brandLogo,
        List<String> images,
        String nameOfGadget,
        int quantity,
        Long articleNumber,
        double rating,
        int percent,
        boolean newProduct,
        boolean recommend,
        BigDecimal price,
        BigDecimal currentPrice,
        String mainColour,
        LocalDate releaseDate,
        int warranty,
        String memory,
        String ram,
        int countSim,
        List<String> uniField,
        boolean likes,
        boolean basket
        ) {
        }