package gadgetarium.dto.response;

import lombok.Builder;

import java.time.LocalDateTime;
import java.util.List;

@Builder
public record FeedbackResponse(
        Long id,
        String gadgetImage,
        String subCategoryName,
        String nameOfGadget,
        Long article,
        String comment,
        List<String> feedbackImages,
        String dateAndTime,
        int rating,
        String fullNameUser,
        String emailUser,
        String responseAdmin
) {
}
