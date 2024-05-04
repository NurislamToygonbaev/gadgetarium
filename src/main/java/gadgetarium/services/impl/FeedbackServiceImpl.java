package gadgetarium.services.impl;

import gadgetarium.dto.request.AdminRequest;
import gadgetarium.dto.request.FeedbackRequest;
import gadgetarium.dto.response.AllFeedbackResponse;
import gadgetarium.dto.response.FeedbackResponse;
import gadgetarium.dto.response.FeedbackStatisticsResponse;
import gadgetarium.dto.response.HttpResponse;
import gadgetarium.entities.*;
import gadgetarium.enums.FeedbackType;
import gadgetarium.enums.ReviewType;
import gadgetarium.enums.Status;
import gadgetarium.exceptions.AlreadyExistsException;
import gadgetarium.exceptions.BadRequestException;
import gadgetarium.repositories.FeedbackRepository;
import gadgetarium.repositories.GadgetRepository;
import gadgetarium.repositories.UserRepository;
import gadgetarium.services.FeedbackService;
import jakarta.transaction.Transactional;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;

import java.util.*;
import java.util.function.Supplier;
import java.util.stream.Collectors;
import java.util.stream.IntStream;

@Slf4j
@Service
@RequiredArgsConstructor
public class FeedbackServiceImpl implements FeedbackService {

    private final FeedbackRepository feedbackRepo;
    private final GadgetRepository gadgetRepo;
    private final CurrentUser currentUserr;

    @Override
    public AllFeedbackResponse getAllFeedbacks(FeedbackType feedbackType) {
        List<Feedback> feedbacks1 = feedbackRepo.findAll();

        long unanswered = feedbacks1.stream()
                .filter(feedback -> feedback.getResponseAdmin() == null)
                .count();

        long totalRatings = feedbacks1.stream()
                .mapToLong(Feedback::getRating)
                .sum();

        List<Feedback> feedbacks = fetchFeedbacksByType(feedbackType);

        if (feedbacks.isEmpty()) {
            return createEmptyResponse();
        }

        Map<Integer, Long> defaultRatingCounts = getRatingCounts(feedbacks1);

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

    @Override
    public FeedbackStatisticsResponse reviewsStatistics(Long gadgetId) {
        Gadget gadgetById = gadgetRepo.getGadgetById(gadgetId);
        int quantityFeedbacks = gadgetById.getFeedbacks().size();

        List<Feedback> feedbacks = gadgetById.getFeedbacks();
        Map<Integer, Long> ratingCounts = getRatingCounts(feedbacks);

        return FeedbackStatisticsResponse
                .builder()
                .overallRating(feedbackRating(gadgetId))
                .quantityFeedbacks(quantityFeedbacks)
                .ratingCounts(ratingCounts)
                .build();
    }

    @Override
    @Transactional
    public HttpResponse leaveFeedback(Long gadgetId, FeedbackRequest feedbackRequest) {
        User currentUser = currentUserr.get();
        Gadget gadgetById = gadgetRepo.getGadgetById(gadgetId);
        Feedback feedback = new Feedback();

        feedback.setRating(feedbackRequest.grade());
        feedback.setDescription(feedbackRequest.comment());
        List<String> images = feedback.getImages();
        images = new ArrayList<>(feedbackRequest.images());
        feedback.setImages(images);

        for (Order order : gadgetById.getOrders()) {
            if (order.getUser().getId().equals(currentUser.getId()) && (order.getStatus().equals(Status.DELIVERED) || order.getStatus().equals(Status.RECEIVED))) {
                currentUser.getFeedbacks().add(feedback);
                feedback.setUser(currentUser);
                gadgetById.getFeedbacks().add(feedback);
                feedback.setGadget(gadgetById);
                double rating = feedbackRating(gadgetId);
                gadgetById.getSubGadget().setRating(rating);
                gadgetById.getSubGadget().setRating(rating);
                feedbackRepo.save(feedback);

                return HttpResponse
                        .builder()
                        .status(HttpStatus.OK)
                        .message("Review sent successfully!")
                        .build();
            }
        }

        return HttpResponse
                .builder()
                .status(HttpStatus.CONFLICT)
                .message("You haven't bought this gadget!")
                .build();
    }

    private Map<Integer, Long> getRatingCounts(List<Feedback> feedbacks) {
        Map<Integer, Long> ratingCounts = initializeDefaultRatingCounts();
        ratingCounts.putAll(feedbacks.stream()
                .collect(Collectors.groupingBy(Feedback::getRating, Collectors.counting())));

        return ratingCounts;
    }

    private Map<Integer, Long> initializeDefaultRatingCounts() {
        Map<Integer, Long> defaultRatingCounts = new HashMap<>();
        for (int rating = 1; rating <= 5; rating++) {
            defaultRatingCounts.put(rating, 0L);
        }
        return defaultRatingCounts;
    }

    private double feedbackRating(Long gadgetId) {
        Gadget gadgetById = gadgetRepo.getGadgetById(gadgetId);
        Map<Integer, Long> ratingCounts = gadgetById.getFeedbacks().stream()
                .collect(Collectors.groupingBy(Feedback::getRating, Collectors.counting()));

        int totalFeedbacks = gadgetById.getFeedbacks().size();
        double averageRating = IntStream.rangeClosed(1, 5)
                                       .mapToDouble(rating -> ratingCounts.getOrDefault(rating, 0L) * rating)
                                       .sum() / totalFeedbacks;

        return Math.floor(averageRating * 10) / 10.0;
    }
}



