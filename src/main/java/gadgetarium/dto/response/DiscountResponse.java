package gadgetarium.dto.response;

import lombok.Builder;
import org.springframework.http.HttpStatus;

@Builder
public record DiscountResponse (
      HttpStatus httpStatus,
      String message
){
}
