package gadgetarium.dto.request;

import gadgetarium.validations.email.EmailValidation;
import gadgetarium.validations.password.PasswordValidation;
import gadgetarium.validations.phoneNumber.PhoneNumberValidation;
import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import lombok.*;

public record SignUpRequest(
        @NotBlank String firstName,
        @NotBlank String lastName,
        String image,
        @PhoneNumberValidation String phoneNumber,
        @Email @EmailValidation String email,
        @PasswordValidation String password,
        String address
) {}
