package gadgetarium.services;

import gadgetarium.dto.request.PasswordRequest;
import gadgetarium.dto.response.HttpResponse;
import gadgetarium.dto.response.SignResponse;

public interface PasswordResetTokenService {
    HttpResponse sendResetEmail(String email);

    SignResponse resetPassword(String token, PasswordRequest request);
}
