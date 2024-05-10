package gadgetarium.api;

import gadgetarium.dto.request.AdminRequest;
import gadgetarium.dto.request.FeedbackRequest;
import gadgetarium.dto.response.AllFeedbackResponse;
import gadgetarium.dto.response.FeedbackResponse;
import gadgetarium.dto.response.FeedbackStatisticsResponse;
import gadgetarium.dto.response.HttpResponse;
import gadgetarium.enums.FeedbackType;
import gadgetarium.services.FeedbackService;
import io.swagger.v3.oas.annotations.Operation;
import jakarta.persistence.PersistenceContext;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.security.access.annotation.Secured;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

@RestController
@RequiredArgsConstructor
@RequestMapping("/api/feedback")
public class FeedbackAPI {

    private final FeedbackService feedbackService;

    @PreAuthorize("hasAuthority('ADMIN')")
    @Operation(description = "Авторизация: Админстратор", summary = "Просмотр всех отзывов")
    @GetMapping("/get-all-feedbacks")
    public AllFeedbackResponse getAllFeedbacks(@RequestParam FeedbackType feedbackType) {
        return feedbackService.getAllFeedbacks(feedbackType);

    }

    @PreAuthorize("hasAuthority('ADMIN')")
    @Operation(description = "Авторизация: Админстратор", summary = "Ответ админстротора на комментарий")
    @PostMapping("/reply-to-comment/{id}")
    public HttpResponse replyToComment(@Valid @RequestBody AdminRequest responseAdmin, @PathVariable Long id) {
        return feedbackService.replyToComment(responseAdmin, id);

    }

    @PreAuthorize("hasAuthority('ADMIN')")
    @Operation(description = "Авторизация: Админстратор", summary = "Редактировать ответ")
    @PutMapping("/edit-comment/{id}")
    public HttpResponse editComment(@Valid @RequestBody AdminRequest responseAdmin, @PathVariable Long id) {
        return feedbackService.editComment(responseAdmin, id);

    }

    @PreAuthorize("hasAuthority('ADMIN')")
    @Operation(description = "Авторизация: Админстратор", summary = "Удалить отзыв")
    @DeleteMapping("/delete-review/{id}")
    public HttpResponse deleteReview(@PathVariable Long id) {
        return feedbackService.deleteReview(id);

    }

    @PreAuthorize("hasAuthority('ADMIN')")
    @Operation(description = "Авторизация: Админстратор", summary = "Смотреть один отзыв ")
    @GetMapping("/get-feedback-by-id/{id}")
    public FeedbackResponse getFeedbackById(@PathVariable Long id) {
        return feedbackService.getFeedbackById(id);

    }

    @PreAuthorize("hasAnyAuthority('USER')")
    @Operation(description = "Авторизация: Пользователь", summary = "Статистика отзывов по гаджету.")
    @GetMapping("/reviews-statistics/{gadgetId}")
    public FeedbackStatisticsResponse reviewsStatistics(@PathVariable Long gadgetId) {
        return feedbackService.reviewsStatistics(gadgetId);
    }

    @PreAuthorize("hasAnyAuthority('USER')")
    @Operation(description = "Авторизация: Пользователь", summary = "Оставить отзыв по ID гаджету.")
    @PostMapping("/leave-feedback/{gadgetId}")
    public HttpResponse leaveFeedback(@PathVariable Long gadgetId,
                                      @RequestBody @Valid FeedbackRequest feedbackRequest) {
        return feedbackService.leaveFeedback(gadgetId, feedbackRequest);
    }

    @PreAuthorize("hasAnyAuthority('USER')")
    @Operation(description = "Авторизация: Пользователь", summary = "обновление отзыва ")
    @PostMapping("/update-feedback/{feedId}")
    public HttpResponse updateFeedback(@PathVariable Long feedId,
                                       @RequestParam String message,
                                       @RequestParam int rating) {
        return feedbackService.updateFeedback(feedId, message, rating);
    }

    @PreAuthorize("hasAnyAuthority('USER')")
    @Operation(description = "Авторизация: Пользователь", summary = "удаление отзыва ")
    @PostMapping("/delete-feedback/{feedId}")
    public HttpResponse deleteFeedback(@PathVariable Long feedId) {
        return feedbackService.deleteFeedback(feedId);
    }
}
