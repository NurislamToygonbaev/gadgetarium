package gadgetarium.dto.request;

import gadgetarium.validations.email.EmailValidation;
import gadgetarium.validations.phoneNumber.PhoneNumberValidation;
import jakarta.validation.constraints.Email;
import lombok.Builder;
import lombok.NonNull;

@Builder
public record ContactRequest(
        @NonNull
        String firstname,
        @NonNull
        String lastname,
        @Email
        @EmailValidation
        String email,
        @PhoneNumberValidation
        String phoneNumber,
        @NonNull
        String message) {
}
