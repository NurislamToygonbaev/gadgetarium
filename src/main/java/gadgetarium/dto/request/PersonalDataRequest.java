package gadgetarium.dto.request;

import gadgetarium.validation.email.EmailValidation;
import gadgetarium.validation.phoneNumber.PhoneNumberValidation;
import lombok.Builder;

@Builder
public record PersonalDataRequest(
        String firstName,
        String lastName,
        @EmailValidation
        String email,
        @PhoneNumberValidation
        String phoneNumber,
        String deliveryAddress
) {
}
