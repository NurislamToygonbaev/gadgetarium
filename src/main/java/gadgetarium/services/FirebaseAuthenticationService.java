package gadgetarium.services;

import gadgetarium.dto.response.SignResponse;

public interface FirebaseAuthenticationService {
    SignResponse authenticateUser(String idToken);
}
