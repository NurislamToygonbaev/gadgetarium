package gadgetarium.services;

import gadgetarium.dto.request.NewsLetterRequest;
import gadgetarium.dto.response.NewsLetterResponse;

public interface MailingService {

    NewsLetterResponse sendNewsLetter(NewsLetterRequest newsLetterRequest);
}
