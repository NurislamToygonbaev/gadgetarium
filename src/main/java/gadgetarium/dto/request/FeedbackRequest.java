package gadgetarium.dto.request;

import gadgetarium.validations.rating.RatingValidation;
import gadgetarium.validations.string.StringValidation;
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
