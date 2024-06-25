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
import gadgetarium.enums.Role;
import gadgetarium.enums.Status;
import gadgetarium.exceptions.AlreadyExistsException;
import gadgetarium.exceptions.BadRequestException;
import gadgetarium.exceptions.NotFoundException;
import gadgetarium.repositories.FeedbackRepository;
import gadgetarium.repositories.GadgetRepository;
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
    private static final int MAX_IMAGES = 5;

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
        Feedback feedback = feedbackRepo.getByIdd(id);
        feedbackRepo.delete(feedback);
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
        if (currentUserr.get().getRole().equals(Role.ADMIN)) {
            feedback.setReviewType(ReviewType.READ);
        }
        Long article = feedback.getGadget().getSubGadgets().stream()
                .map(SubGadget::getArticle)
                .findFirst()
                .orElseThrow(() -> new NotFoundException("Not found feedback!"));

        String image = feedback.getImages() != null && !feedback.getImages().isEmpty() ? feedback.getImages().getFirst() : null;
        return FeedbackResponse.builder()
                .id(feedback.getId())
                .gadgetImage(image)
                .subCategoryName(feedback.getGadget().getSubCategory().getSubCategoryName())
                .nameOfGadget(feedback.getGadget().getNameOfGadget())
                .article(article)
                .comment(feedback.getDescription())
                .feedbackImages(feedback.getImages())
                .dateAndTime(String.valueOf(feedback.getDateAndTime()))
                .rating(feedback.getRating())
                .fullNameUser(feedback.getUser().getFirstName() + " " + feedback.getUser().getLastName())
                .emailUser(feedback.getUser().getEmail())
                .responseAdmin(feedback.getResponseAdmin())
                .reviewType(feedback.getReviewType().name())
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
        if (gadget == null) {
            throw new NotFoundException("gadget not found!");
        }
        User user = feedback.getUser();

        String firstImage = getSafely(() -> {
            if (gadget.getSubGadgets() != null && !gadget.getSubGadgets().isEmpty()) {
                return !gadget.getSubGadgets().getFirst().getImages().isEmpty() ? gadget.getSubGadgets().getFirst().getImages().getFirst() : null;
            }
            return null;
        });

        String subCategoryName = getSafely(() -> gadget.getSubCategory() != null ? gadget.getSubCategory().getSubCategoryName() : null);
        String gadgetName = getSafely(gadget::getNameOfGadget);
        String userEmail = getSafely(user::getEmail);
        String adminResponse = feedback.getResponseAdmin();

        String firstName = getSafely(user::getFirstName);
        String lastName = getSafely(user::getLastName);
        String fullName = (firstName != null ? firstName : "") + (lastName != null ? " " + lastName : "");

        Long article = getSafely(() -> {
            if (gadget.getSubGadgets() != null && !gadget.getSubGadgets().isEmpty()) {
                return gadget.getSubGadgets().get(0).getArticle();
            }
            return null;
        });

        return new FeedbackResponse(
                feedback.getId(),
                firstImage,
                subCategoryName,
                gadgetName,
                article,
                feedback.getDescription(),
                feedback.getImages(),
                String.valueOf(feedback.getDateAndTime()),
                feedback.getRating(),
                fullName.trim(),
                userEmail,
                adminResponse,
                feedback.getReviewType().name()
        );
    }

    private AllFeedbackResponse createAllFeedbackResponse(long totalRatings, long unanswered, Map<Integer, Long> ratingCounts, List<FeedbackResponse> feedbackResponses) {
        return AllFeedbackResponse.builder()
                .totalRatings(totalRatings)
                .ratingCounts(ratingCounts)
                .unanswered(unanswered)
                .feedbackResponseList(feedbackResponses.stream().sorted(Comparator.comparingInt(FeedbackResponse::rating).reversed()).toList())
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
        if (feedbackRequest.images().size() > MAX_IMAGES) {
            throw new BadRequestException("Cannot have more than 5 images");
        }

        User currentUser = currentUserr.get();
        Gadget gadget = gadgetRepo.getGadgetById(gadgetId);
        Feedback feedback = new Feedback();
        feedback.setRating(feedbackRequest.grade());
        feedback.setDescription(feedbackRequest.comment());

        boolean hasPurchased = false;

        for (SubGadget subGadget : gadget.getSubGadgets()) {
            for (Order order : subGadget.getOrders()) {
                if (order.getUser().getId().equals(currentUser.getId()) &&
                    (order.getStatus().equals(Status.DELIVERED) || order.getStatus().equals(Status.RECEIVED))) {
                    hasPurchased = true;
                    break;
                }
            }
            if (hasPurchased) break;
        }

        if (hasPurchased) {
            currentUser.getFeedbacks().add(feedback);
            feedback.setUser(currentUser);
            gadget.getFeedbacks().add(feedback);
            feedback.setGadget(gadget);

            if (!feedbackRequest.images().isEmpty()) {
                feedback.setImages(feedbackRequest.images());
            }

            double rating = feedbackRating(gadgetId);
            gadget.setRating(rating);
            feedbackRepo.save(feedback);
            gadgetRepo.save(gadget);

            return HttpResponse.builder()
                    .status(HttpStatus.OK)
                    .message("Review sent successfully!")
                    .build();
        }

        return HttpResponse.builder()
                .status(HttpStatus.BAD_REQUEST)
                .message("You haven't bought this gadget!")
                .build();
    }

    @Override
    @Transactional
    public HttpResponse deleteFeedback(Long feedId) {
        User user = currentUserr.get();
        Feedback feedback = findFeedbackById(user, feedId);

        user.getFeedbacks().remove(feedback);
        feedbackRepo.delete(feedback);

        log.info("Feedback deleted successfully for id: " + feedId);
        return HttpResponse.builder()
                .status(HttpStatus.OK)
                .message("Successfully deleted")
                .build();
    }

    @Override
    @Transactional
    public HttpResponse updateUserFeedback(Long feedId, FeedbackRequest feedbackRequest) {
        User user = currentUserr.get();
        Feedback feedback = findFeedbackById(user, feedId);

        feedback.setRating(feedbackRequest.grade());
        feedback.setDescription(feedbackRequest.comment());

        List<String> images = feedbackRequest.images();
        if (images == null) {
            log.warn("Images list is null in feedback request");
            throw new BadRequestException("Images list cannot be null");
        }

        if (images.size() > MAX_IMAGES) {
            log.warn("Too many images in feedback request: " + images.size());
            throw new BadRequestException("Cannot have more than " + MAX_IMAGES + " images");
        }

        feedback.setImages(images);

        log.info("Feedback updated successfully for id: " + feedId);
        feedbackRepo.save(feedback);

        return HttpResponse.builder()
                .status(HttpStatus.OK)
                .message("Successfully updated")
                .build();
    }

    private Feedback findFeedbackById(User user, Long feedId) {
        if (user.getFeedbacks() == null) {
            log.warn("Feedbacks not found for user: " + user.getId());
            throw new NotFoundException("Feedbacks not found");
        }

        Optional<Feedback> optionalFeedback = user.getFeedbacks().stream()
                .filter(feedback -> feedback.getId().equals(feedId))
                .findFirst();

        if (optionalFeedback.isEmpty()) {
            log.warn("Feedback not found for id: " + feedId);
            throw new NotFoundException("Feedback not found");
        }

        Feedback feedback = optionalFeedback.get();
        checkResponse(feedback);

        return feedback;
    }

    private void checkResponse(Feedback feedback) {
        if (feedback.getResponseAdmin() != null) {
            log.warn("Attempt to update feedback with admin response: " + feedback.getId());
            throw new BadRequestException("Can't update or delete the feedback");
        }
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



