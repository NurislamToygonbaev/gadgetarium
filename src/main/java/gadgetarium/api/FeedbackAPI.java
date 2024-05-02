package gadgetarium.api;

import gadgetarium.dto.request.AdminRequest;
import gadgetarium.dto.response.AllFeedbackResponse;
import gadgetarium.dto.response.FeedbackResponse;
import gadgetarium.dto.response.HttpResponse;
import gadgetarium.enums.FeedbackType;
import gadgetarium.services.FeedbackService;
import io.swagger.v3.oas.annotations.Operation;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
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
    public FeedbackResponse getFeedbackById(@PathVariable Long id){
        return feedbackService.getFeedbackById(id);

    }
}
