package gadgetarium.api;

import gadgetarium.dto.request.NewsLetterRequest;
import gadgetarium.dto.response.NewsLetterResponse;
import gadgetarium.services.MailingService;
import io.swagger.v3.oas.annotations.Operation;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.security.access.annotation.Secured;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

@Slf4j
@RestController
@RequestMapping("/api/news-letter")
@RequiredArgsConstructor
@CrossOrigin(origins = "*", maxAge = 100000L)
public class NewsLetterApi {

    private final MailingService mailingService;

    @PreAuthorize("hasAuthority('ADMIN')")
    @Operation(summary = "Создать рассылку", description = "Авторизация: АДМИНСТРАТОР")
    @PostMapping("/send")
    public NewsLetterResponse sendNewsLetter(@RequestBody @Valid NewsLetterRequest newsLetterRequest){
        return mailingService.sendNewsLetter(newsLetterRequest);
    }
}
