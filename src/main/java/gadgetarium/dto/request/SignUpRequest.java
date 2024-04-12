package gadgetarium.dto.request;

import gadgetarium.validation.email.EmailValidation;
import gadgetarium.validation.password.PasswordValidation;
import gadgetarium.validation.phoneNumber.PhoneNumberValidation;
import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import lombok.Builder;
import lombok.Getter;
import lombok.Setter;

@Builder
@Getter
@Setter
public class SignUpRequest{

        @NotBlank
        private String firstName;
        @NotBlank
        private String lastName;
        private String image;
        @PhoneNumberValidation
        private String phoneNumber;
        @Email
        @EmailValidation
        private String email;
        @PasswordValidation
        private String password;
        private String address;
}
