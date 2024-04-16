package gadgetarium.dto.response;

import gadgetarium.enums.Discount;
import gadgetarium.enums.Sort;
import lombok.Builder;

import java.util.List;

@Builder
public record ResultPaginationGadget(
        Sort sort,
        Discount discount,
        int page,
        int size,
        List<PaginationGadget> paginationGadgets
) {
}
