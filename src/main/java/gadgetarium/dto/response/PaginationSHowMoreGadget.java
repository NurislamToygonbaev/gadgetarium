package gadgetarium.dto.response;

import gadgetarium.enums.Discount;
import gadgetarium.enums.Memory;
import gadgetarium.enums.Ram;
import gadgetarium.enums.Sort;
import lombok.Builder;

import java.math.BigDecimal;
import java.util.List;

@Builder
public record PaginationSHowMoreGadget(
        Sort sort,
        Discount discount,
        int page,
        int size,
        String brand,
        BigDecimal costFrom,
        BigDecimal costUpTo,
        String colour,
        Memory memory,
        Ram ram,
        List<GadgetsResponse> responses
) {
}
