package gadgetarium.dto.response;

import lombok.Builder;

import java.util.List;

@Builder
public record AllFeedbackResponse(
        long totalRatings,
        List<FeedbackResponse> feedbackResponseList


){}
