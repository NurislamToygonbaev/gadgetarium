package gadgetarium.services;

import gadgetarium.dto.request.EmailRequest;
import gadgetarium.dto.request.NewsLetterRequest;
import gadgetarium.dto.response.HttpResponse;
import gadgetarium.dto.response.NewsLetterResponse;

public interface MailingService {

    NewsLetterResponse sendNewsLetter(NewsLetterRequest newsLetterRequest);

    HttpResponse followUs(EmailRequest emailRequest);
}
