package gadgetarium.dto.request;

import gadgetarium.validations.email.EmailValidation;
import gadgetarium.validations.phoneNumber.PhoneNumberValidation;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import lombok.Builder;

@Builder
public record CurrentUserProfileRequest(
        @NotBlank
        String userName,
        @NotNull
        String lastName,
        @EmailValidation
        String email,
        @PhoneNumberValidation
        String phoneNumber,
        @NotNull
        String address
){}
