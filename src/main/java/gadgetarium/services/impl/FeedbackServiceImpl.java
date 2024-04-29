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
import gadgetarium.exceptions.AlreadyExistsException;
import gadgetarium.repositories.FeedbackRepository;
import gadgetarium.services.FeedbackService;
import jakarta.transaction.Transactional;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.function.Supplier;

@Slf4j
@Service
@RequiredArgsConstructor
public class FeedbackServiceImpl implements FeedbackService {

    private final FeedbackRepository feedbackRepo;

    @Override
    public AllFeedbackResponse getAllFeedbacks(FeedbackType feedbackType) {

        long totalRatings = 0;
        List<Feedback> feedbacks1 = feedbackRepo.findAll();
        totalRatings = feedbacks1.stream().mapToLong(Feedback::getRating).sum();

        List<Feedback> feedbacks = fetchFeedbacksByType(feedbackType);

        if (feedbacks.isEmpty()) {
            return createEmptyResponse();
        }

        List<FeedbackResponse> feedbackResponses = new ArrayList<>(feedbacks.size());

        for (Feedback feedback : feedbacks) {

            FeedbackResponse feedbackResponse = convertFeedbackToResponse(feedback);
            feedbackResponses.add(feedbackResponse);
        }

        return createAllFeedbackResponse(totalRatings, feedbackResponses);
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
        feedback.setResponseAdmin(adminRequest.responseAdmin());
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

        String firstImage = getSafely(() -> {
            assert subGadget != null;
            return subGadget.getImages().getFirst();
        });
        String subCategoryName = getSafely(() -> gadget.getSubCategory().getSubCategoryName());
        String gadgetName = getSafely(() -> {
            assert subGadget != null;
            return subGadget.getNameOfGadget();
        });
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

    private AllFeedbackResponse createAllFeedbackResponse(long totalRatings, List<FeedbackResponse> feedbackResponses){

        return AllFeedbackResponse.builder()
                .totalRatings(totalRatings)
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
            log.error("Error in getSafely", e);
            return null;
        }
    }

//      if (isUnanswered) {
//        unansweredCount++;
//    }
//    boolean isUnanswered = isFeedbackUnanswered(feedback);
//    private boolean isFeedbackUnanswered(Feedback feedback) {
//        String adminResponse = feedback.getResponseAdmin();
//        return adminResponse == null || adminResponse.isEmpty();
//    }
}



