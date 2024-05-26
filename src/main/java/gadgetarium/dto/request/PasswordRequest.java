package gadgetarium.dto.request;

import gadgetarium.validations.password.PasswordValidation;
import lombok.Builder;

@Builder
public record PasswordRequest(
        @PasswordValidation
        String password,
        @PasswordValidation
        String confirmPassword
) {
}
