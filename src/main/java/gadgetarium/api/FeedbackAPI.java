package gadgetarium.api;

import gadgetarium.dto.request.AdminRequest;
import gadgetarium.dto.response.AllFeedbackResponse;
import gadgetarium.dto.response.HttpResponse;
import gadgetarium.enums.FeedbackType;
import gadgetarium.services.FeedbackService;
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
    @GetMapping("/get-all-feedbacks")
    public AllFeedbackResponse getAllFeedbacks(@RequestParam FeedbackType feedbackType) {
        return feedbackService.getAllFeedbacks(feedbackType);
    }

    @Secured("ADMIN")
    @PostMapping("/reply-to-comment/{id}")
    public HttpResponse replyToComment(@Valid @RequestBody AdminRequest responseAdmin, @PathVariable Long id) {
        return feedbackService.replyToComment(responseAdmin, id);
    }

    @Secured("ADMIN")
    @PutMapping("/edit-comment/{id}")
    public HttpResponse editComment(@Valid @RequestBody AdminRequest responseAdmin, @PathVariable Long id){
        return feedbackService.editComment(responseAdmin, id);
    }

    @Secured("ADMIN")
    @DeleteMapping("/delete-review/{id}")
    public HttpResponse deleteReview(@PathVariable Long id){
        return feedbackService.deleteReview(id);
    }
}
