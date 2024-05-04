package gadgetarium.services;

import gadgetarium.dto.request.AdminRequest;
import gadgetarium.dto.request.FeedbackRequest;
import gadgetarium.dto.response.AllFeedbackResponse;
import gadgetarium.dto.response.FeedbackResponse;
import gadgetarium.dto.response.FeedbackStatisticsResponse;
import gadgetarium.dto.response.HttpResponse;
import gadgetarium.enums.FeedbackType;

public interface FeedbackService {
    AllFeedbackResponse getAllFeedbacks(FeedbackType feedbackType);

    HttpResponse replyToComment(AdminRequest responseAdmin, Long id);

    HttpResponse editComment(AdminRequest adminRequest, Long id);

    HttpResponse deleteReview(Long id);

    FeedbackResponse getFeedbackById(Long id);

    FeedbackStatisticsResponse reviewsStatistics(Long gadgetId);

    HttpResponse leaveFeedback(Long gadgetId, FeedbackRequest feedbackRequest);
}
