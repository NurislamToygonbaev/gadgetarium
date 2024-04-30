package gadgetarium.services.impl;

import gadgetarium.dto.request.AdminRequest;
import gadgetarium.dto.response.AllFeedbackResponse;
import gadgetarium.dto.response.FeedbackResponse;
import gadgetarium.dto.response.HttpResponse;
import gadgetarium.entities.Feedback;
import gadgetarium.entities.Gadget;
import gadgetarium.entities.SubGadget;
import gadgetarium.entities.User;
import gadgetarium.enums.FeedbackType;
import gadgetarium.enums.ReviewType;
import gadgetarium.exceptions.AlreadyExistsException;
import gadgetarium.exceptions.BadRequestException;
import gadgetarium.repositories.FeedbackRepository;
import gadgetarium.services.FeedbackService;
import jakarta.transaction.Transactional;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;

import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.function.Supplier;
import java.util.stream.Collectors;

@Slf4j
@Service
@RequiredArgsConstructor
public class FeedbackServiceImpl implements FeedbackService {

    private final FeedbackRepository feedbackRepo;

    @Override
    public AllFeedbackResponse getAllFeedbacks(FeedbackType feedbackType) {
        List<Feedback> feedbacks1 = feedbackRepo.findAll();

        List<Feedback> feedbacks = fetchFeedbacksByType(feedbackType);

        if (feedbacks.isEmpty()) {
            return createEmptyResponse();
        }
        long unanswered = feedbacks1.stream()
                .filter(feedback -> feedback.getResponseAdmin() == null)
                .count();

        long totalRatings = feedbacks1.stream()
                .mapToLong(Feedback::getRating)
                .sum();

        Map<Integer, Long> defaultRatingCounts = new HashMap<>();
        for (int rating = 1; rating <= 5; rating++) {
            defaultRatingCounts.put(rating, 0L);
        }

        defaultRatingCounts.putAll(feedbacks1.stream()
                .collect(Collectors.groupingBy(Feedback::getRating, Collectors.counting())));
        List<FeedbackResponse> feedbackResponses = feedbacks.stream()
                .map(this::convertFeedbackToResponse)
                .collect(Collectors.toList());

        return createAllFeedbackResponse(totalRatings, unanswered, defaultRatingCounts, feedbackResponses);
    }

    @Transactional
    @Override
    public HttpResponse replyToComment(AdminRequest responseAdmin, Long id) {

        Feedback feedback = feedbackRepo.getByIdd(id);

        if (feedback.getResponseAdmin() != null && !feedback.getResponseAdmin().isEmpty()) {
            throw new AlreadyExistsException("Admin has already responded to this comment");
        }

        feedback.setResponseAdmin(responseAdmin.responseAdmin());
        feedbackRepo.save(feedback);
        log.info("Reply to comment sent successfully");

        return HttpResponse.builder()
                .status(HttpStatus.OK)
                .message("Reply to comment sent successfully")
                .build();
    }

    @Transactional
    @Override
    public HttpResponse editComment(AdminRequest adminRequest, Long id) {

        Feedback feedback = feedbackRepo.getByIdd(id);
        if (feedback.getResponseAdmin() == null || feedback.getResponseAdmin().isEmpty()) {
            throw new BadRequestException("This review has not yet been answered");
        }
        feedback.setResponseAdmin(adminRequest.responseAdmin());
        feedbackRepo.save(feedback);
        log.info("Comment successfully edited!");
        return HttpResponse.builder()
                .status(HttpStatus.OK)
                .message("Comment successfully edited!")
                .build();
    }

    @Override
    public HttpResponse deleteReview(Long id) {

        feedbackRepo.delete(feedbackRepo.getByIdd(id));
        log.info("Review successfully deleted!");
        return HttpResponse.builder()
                .status(HttpStatus.OK)
                .message("Review successfully deleted!")
                .build();
    }

    @Override
    @Transactional
    public FeedbackResponse getFeedbackById(Long id) {
        Feedback feedback = feedbackRepo.getByIdd(id);
        feedback.setReviewType(ReviewType.READ);
        return FeedbackResponse.builder()
                .id(feedback.getId())
                .gadgetImage(feedback.getImages().getFirst())
                .subCategoryName(feedback.getGadget().getSubCategory().getSubCategoryName())
                .nameOfGadget(feedback.getGadget().getSubGadget().getNameOfGadget())
                .article(feedback.getGadget().getArticle())
                .comment(feedback.getDescription())
                .feedbackImages(feedback.getImages())
                .dateAndTime(feedback.getDateAndTime())
                .rating(feedback.getRating())
                .fullNameUser(feedback.getUser().getFirstName() + " " + feedback.getUser().getLastName())
                .emailUser(feedback.getUser().getEmail())
                .responseAdmin(feedback.getResponseAdmin())
                .build();
    }

    private List<Feedback> fetchFeedbacksByType(FeedbackType feedbackType) {
        return switch (feedbackType) {
            case UNANSWERED -> feedbackRepo.findUnansweredFeedbacks();
            case ANSWERED -> feedbackRepo.findAnsweredFeedbacks();
            default -> feedbackRepo.findAll();
        };
    }

    private FeedbackResponse convertFeedbackToResponse(Feedback feedback) {
        Gadget gadget = feedback.getGadget();
        SubGadget subGadget = getSafely(gadget::getSubGadget);
        User user = feedback.getUser();

        String firstImage = getSafely(() -> subGadget != null ? subGadget.getImages().getFirst() : null);
        String subCategoryName = getSafely(() -> gadget.getSubCategory().getSubCategoryName());
        String gadgetName = getSafely(() -> subGadget != null ? subGadget.getNameOfGadget() : null);
        String userEmail = getSafely(user::getEmail);
        String adminResponse = feedback.getResponseAdmin();

        String firstName = getSafely(user::getFirstName);
        String lastName = getSafely(user::getLastName);
        String fullName = (firstName != null && lastName != null)
                ? firstName + " " + lastName
                : (firstName != null ? firstName : "") + (lastName != null ? lastName : "");

        return new FeedbackResponse(
                feedback.getId(),
                firstImage,
                subCategoryName,
                gadgetName,
                getSafely(gadget::getArticle),
                feedback.getDescription(),
                feedback.getImages(),
                feedback.getDateAndTime(),
                feedback.getRating(),
                fullName,
                userEmail,
                adminResponse
        );
    }

    private AllFeedbackResponse createAllFeedbackResponse(long totalRatings, long unanswered, Map<Integer, Long> ratingCounts, List<FeedbackResponse> feedbackResponses) {
        return AllFeedbackResponse.builder()
                .totalRatings(totalRatings)
                .ratingCounts(ratingCounts)
                .unanswered(unanswered)
                .feedbackResponseList(feedbackResponses)
                .build();
    }

    private AllFeedbackResponse createEmptyResponse() {
        return AllFeedbackResponse.builder()
                .totalRatings(0)
                .feedbackResponseList(Collections.emptyList())
                .build();
    }

    private <T> T getSafely(Supplier<T> supplier) {
        try {
            return supplier.get();
        } catch (Exception e) {
            log.error("Error in getSafely in class");
            return null;
        }

    }

}



