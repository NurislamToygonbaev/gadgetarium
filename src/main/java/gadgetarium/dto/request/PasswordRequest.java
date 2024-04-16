package gadgetarium.dto.request;

import gadgetarium.validation.password.PasswordValidation;
import lombok.Builder;

@Builder
public record PasswordRequest(
        @PasswordValidation
        String password,
        @PasswordValidation
        String confirmPassword
) {
}
