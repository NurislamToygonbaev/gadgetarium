package gadgetarium.dto.response;

import lombok.Builder;

import java.util.List;
import java.util.Map;

@Builder
public record AllFeedbackResponse(
        long totalRatings,
        long unanswered,
        Map<Integer, Long> ratingCounts,
        List<FeedbackResponse> feedbackResponseList

) {
}
