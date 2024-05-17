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
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/news-letter")
@RequiredArgsConstructor
@Slf4j
public class NewsLetterApi {

    private final MailingService mailingService;

    @PreAuthorize("hasAuthority('ADMIN')")
    @Operation(summary = "Создать рассылку", description = "Авторизация: АДМИНСТРАТОР")
    @PostMapping
    public NewsLetterResponse sendNewsLetter(@RequestBody @Valid NewsLetterRequest newsLetterRequest){
        return mailingService.sendNewsLetter(newsLetterRequest);
    }
}
