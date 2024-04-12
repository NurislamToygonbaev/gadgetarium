package gadgetarium.dto.response;

import lombok.Builder;
import org.springframework.http.HttpStatus;

@Builder
public record SignResponse(
        Long id,
        String email,
        String token,
        HttpResponse response
) {
}
