package gadgetarium.dto.request;

import gadgetarium.enums.Discount;
import gadgetarium.enums.Sort;
import gadgetarium.validation.number.NumberValidation;
import lombok.Builder;

@Builder
public record PaginationRequest(
        Sort sort,
        Discount discount,
        @NumberValidation
        int page,
        @NumberValidation
        int size
) {
}
