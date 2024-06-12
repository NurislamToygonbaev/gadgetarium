package gadgetarium.apies;

import gadgetarium.dto.request.AdminRequest;
import gadgetarium.dto.request.FeedbackRequest;
import gadgetarium.dto.response.AllFeedbackResponse;
import gadgetarium.dto.response.FeedbackResponse;
import gadgetarium.dto.response.FeedbackStatisticsResponse;
import gadgetarium.dto.response.HttpResponse;
import gadgetarium.enums.FeedbackType;
import gadgetarium.services.FeedbackService;
import io.swagger.v3.oas.annotations.Operation;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

@Slf4j
@RestController
@RequiredArgsConstructor
@RequestMapping("/api/feedback")
@CrossOrigin(origins = "*", maxAge = 3600)
public class FeedbackApi {

    private final FeedbackService feedbackService;

    @PreAuthorize("hasAuthority({'ADMIN'})")
    @Operation(description = "Авторизация: АДМИНСТРАТОР", summary = "Просмотр всех отзывов")
    @GetMapping
    public AllFeedbackResponse getAllFeedbacks(@RequestParam FeedbackType feedbackType) {
        return feedbackService.getAllFeedbacks(feedbackType);
    }

    @PreAuthorize("hasAuthority('ADMIN')")
    @Operation(description = "Авторизация: АДМИНСТРАТОР", summary = "Ответ админстротора на комментарий")
    @PatchMapping("/reply/{id}")
    public HttpResponse replyToComment(@Valid @RequestBody AdminRequest responseAdmin, @PathVariable Long id) {
        return feedbackService.replyToComment(responseAdmin, id);
    }

    @PreAuthorize("hasAuthority('ADMIN')")
    @Operation(description = "Авторизация: АДМИНСТРАТОР", summary = "Редактировать ответ")
    @PatchMapping("/{id}")
    public HttpResponse editComment(@Valid @RequestBody AdminRequest responseAdmin, @PathVariable Long id) {
        return feedbackService.editComment(responseAdmin, id);
    }

    @PreAuthorize("hasAuthority('ADMIN')")
    @Operation(description = "Авторизация: АДМИНСТРАТОР", summary = "Удалить отзыв")
    @DeleteMapping("/{id}")
    public HttpResponse deleteReview(@PathVariable Long id) {
        return feedbackService.deleteReview(id);

    }

    @PreAuthorize("hasAuthority({'ADMIN'})")
    @Operation(description = "Авторизация: АДМИНИСТРАТОР", summary = "Смотреть один отзыв ")
    @GetMapping("/by-id/{id}")
    public FeedbackResponse getFeedbackById(@PathVariable Long id) {
        return feedbackService.getFeedbackById(id);

    }
    @Operation(description = "Авторизация: ВСЕ", summary = "Статистика отзывов по гаджету.")
    @GetMapping("/statistics/{gadgetId}")
    public FeedbackStatisticsResponse reviewsStatistics(@PathVariable Long gadgetId) {
        return feedbackService.reviewsStatistics(gadgetId);
    }

    @PreAuthorize("hasAnyAuthority('USER')")
    @Operation(description = "Авторизация: ПОЛЬЗОВАТЕЛЬ", summary = "Оставить отзыв по ID гаджету.")
    @PostMapping("/{gadgetId}")
    public HttpResponse leaveFeedback(@PathVariable Long gadgetId,
                                      @RequestBody @Valid FeedbackRequest feedbackRequest) {
        return feedbackService.leaveFeedback(gadgetId, feedbackRequest);
    }

    @PreAuthorize("hasAnyAuthority('USER')")
    @Operation(description = "Авторизация: ПОЛЬЗОВАТЕЛЬ", summary = "Удаление своего отзыва ")
    @DeleteMapping("/delete-feedback/{feedId}")
    public HttpResponse deleteFeedback(@PathVariable Long feedId) {
        return feedbackService.deleteFeedback(feedId);
    }

    @PreAuthorize("hasAnyAuthority('USER')")
    @Operation(description = "Авторизация: ПОЛЬЗОВАТЕЛЬ", summary = "Обновление своего отзыва ")
    @PatchMapping ("/edit/{feedId}")
    public HttpResponse editFeedback(@PathVariable Long feedId,
                                     @RequestBody FeedbackRequest feedbackRequest){
        return feedbackService.updateUserFeedback(feedId, feedbackRequest);
    }
}
