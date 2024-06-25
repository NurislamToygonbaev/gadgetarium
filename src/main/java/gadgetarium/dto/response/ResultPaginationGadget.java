package gadgetarium.dto.response;

import gadgetarium.enums.Discount;
import gadgetarium.enums.Sort;
import lombok.Builder;

import java.time.LocalDate;
import java.util.List;

@Builder
public record ResultPaginationGadget(
        LocalDate startDate,
        LocalDate endDate,
        String keyword,
        Sort sort,
        Discount discount,
        int allProduct,
        int onSale,
        int inFavorites,
        int inBasket,
        int page,
        int size,
        List<PaginationGadget> paginationGadgets
) {
}
