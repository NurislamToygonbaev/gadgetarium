package gadgetarium.dto.response;

import gadgetarium.enums.Memory;
import lombok.Builder;
import org.hibernate.boot.registry.StandardServiceRegistry;

import java.math.BigDecimal;

@Builder
public record AllFavoritesResponse(
        Long id,
        String image,
        String category,
        String brandName,
        String nameOfGadget,
        Memory memory,
        String color,
        double rating,
        BigDecimal price,
        BigDecimal currentPrice

) {
}