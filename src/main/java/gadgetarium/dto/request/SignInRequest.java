package gadgetarium.dto.request;

import gadgetarium.validation.email.EmailValidation;
import gadgetarium.validation.password.PasswordValidation;
import jakarta.validation.constraints.Email;

public record SignInRequest(
        @Email
        @EmailValidation
        String email,
        @PasswordValidation
        String password
) {
}
