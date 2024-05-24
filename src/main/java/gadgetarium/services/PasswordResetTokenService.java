package gadgetarium.services;

import gadgetarium.dto.response.HttpResponse;
import gadgetarium.dto.response.SignResponse;

public interface PasswordResetTokenService {
    HttpResponse sendResetEmail(String email);

    SignResponse resetPassword(String token, String password, String confirmPassword);
}
