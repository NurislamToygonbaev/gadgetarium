package gadgetarium.dto.response;

import lombok.Builder;
import org.springframework.http.HttpStatus;

@Builder
public record SignResponse(
        HttpStatus httpStatus,
        String token,
        String message
) {
}
