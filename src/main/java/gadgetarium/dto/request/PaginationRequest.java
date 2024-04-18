package gadgetarium.dto.request;

import gadgetarium.validation.number.NumberValidation;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotEmpty;
import lombok.Builder;


@Builder
public record PaginationRequest(
        @NumberValidation
        int page,
        @NumberValidation
        int size
) {
}
