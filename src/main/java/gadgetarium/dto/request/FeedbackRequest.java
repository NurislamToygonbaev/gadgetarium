package gadgetarium.dto.request;

import gadgetarium.validation.feedack.ImageValidation;
import gadgetarium.validation.rating.RatingValidation;
import gadgetarium.validation.string.StringValidation;
import jakarta.persistence.ElementCollection;
import jakarta.validation.constraints.NotBlank;
import lombok.Builder;

import java.util.List;

@Builder
public record FeedbackRequest(
        @RatingValidation
        int grade,
        @StringValidation
        String comment,
        List<String> images
) {
}
