package gadgetarium.dto.request;

import gadgetarium.validations.email.EmailValidation;
import jakarta.validation.constraints.Email;
import lombok.Builder;

@Builder
public record EmailRequest(
        @Email
        @EmailValidation
        String email
) {
}
