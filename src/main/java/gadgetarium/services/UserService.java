package gadgetarium.services;

import gadgetarium.dto.request.PasswordRequest;
import gadgetarium.dto.request.SignInRequest;
import gadgetarium.dto.request.SignUpRequest;
import gadgetarium.dto.response.HttpResponse;
import gadgetarium.dto.response.SignResponse;
import jakarta.mail.MessagingException;

public interface UserService {

    SignResponse signUp(SignUpRequest signUpRequest);

    SignResponse signIn(SignInRequest signInRequest);
}
