package gadgetarium.api;

import gadgetarium.dto.request.AdminRequest;
import gadgetarium.dto.response.AllFeedbackResponse;
import gadgetarium.dto.response.FeedbackResponse;
import gadgetarium.dto.response.HttpResponse;
import gadgetarium.entities.Feedback;
import gadgetarium.enums.FeedbackType;
import gadgetarium.services.FeedbackService;
import io.swagger.v3.oas.annotations.Operation;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.security.access.annotation.Secured;
import org.springframework.web.bind.annotation.*;

@RestController
@RequiredArgsConstructor
@RequestMapping("/api/feedback")
public class FeedbackAPI {

    private final FeedbackService feedbackService;

    @Secured("ADMIN")
    @Operation(summary = "Просмотр всех отзывов")
    @GetMapping("/get-all-feedbacks")
    public AllFeedbackResponse getAllFeedbacks(@RequestParam FeedbackType feedbackType) {
        return feedbackService.getAllFeedbacks(feedbackType);
    }

    @Secured("ADMIN")
    @Operation(summary = "Ответ админстротора на коммент")
    @PostMapping("/reply-to-comment/{id}")
    public HttpResponse replyToComment(@Valid @RequestBody AdminRequest responseAdmin, @PathVariable Long id) {
        return feedbackService.replyToComment(responseAdmin, id);
    }

    @Secured("ADMIN")
    @Operation(summary = "Редактировать ответ")
    @PutMapping("/edit-comment/{id}")
    public HttpResponse editComment(@Valid @RequestBody AdminRequest responseAdmin, @PathVariable Long id) {
        return feedbackService.editComment(responseAdmin, id);
    }

    @Secured("ADMIN")
    @Operation(summary = "Удалить отзыв")
    @DeleteMapping("/delete-review/{id}")
    public HttpResponse deleteReview(@PathVariable Long id) {
        return feedbackService.deleteReview(id);
    }

    @Secured("ADMIN")
    @Operation(summary = "")
    @GetMapping("/get-feedback-by-id/{id}")
    public FeedbackResponse getFeedbackById(@PathVariable Long id){
        return feedbackService.getFeedbackById(id);
    }
}
