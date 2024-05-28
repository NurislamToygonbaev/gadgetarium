package gadgetarium.dto.request;

import gadgetarium.validations.email.EmailValidation;
import gadgetarium.validations.password.PasswordValidation;
import jakarta.validation.constraints.Email;

public record SignInRequest(
        @Email
        @EmailValidation
        String email,
        @PasswordValidation
        String password
) {
}
