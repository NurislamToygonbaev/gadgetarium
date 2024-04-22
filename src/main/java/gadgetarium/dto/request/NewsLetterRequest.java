package gadgetarium.dto.request;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import lombok.Builder;

import java.time.LocalDate;

@Builder
public record NewsLetterRequest (
        String image,
        @NotBlank
        String nameOfNewsLetter,
        String description,
        @NotNull
        LocalDate startDateOfDiscount,
        @NotNull
        LocalDate endDateOfDiscount
){
}
