package gadgetarium.dto.request;

import gadgetarium.validations.number.NumberValidation;
import lombok.Builder;


@Builder
public record PaginationRequest(
        @NumberValidation
        int page,
        @NumberValidation
        int size
) {
}
