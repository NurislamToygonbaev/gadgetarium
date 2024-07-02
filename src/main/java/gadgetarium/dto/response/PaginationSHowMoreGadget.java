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
        List<String> brand,
        BigDecimal costFrom,
        BigDecimal costUpTo,
        List<String> colour,
        List<Memory> memory,
        List<Ram> ram,
        List<GadgetsResponse> responses
) {
}
