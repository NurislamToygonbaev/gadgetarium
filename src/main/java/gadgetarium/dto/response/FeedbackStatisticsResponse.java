package gadgetarium.dto.response;

import lombok.Builder;

import java.util.Map;

@Builder
public record FeedbackStatisticsResponse(
        double overallRating,
        int quantityFeedbacks,
        Map<Integer, Long> ratingCounts
) {
}
