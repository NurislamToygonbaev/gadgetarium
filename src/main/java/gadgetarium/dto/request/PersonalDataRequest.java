package gadgetarium.dto.request;

import gadgetarium.validations.email.EmailValidation;
import gadgetarium.validations.phoneNumber.PhoneNumberValidation;
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
