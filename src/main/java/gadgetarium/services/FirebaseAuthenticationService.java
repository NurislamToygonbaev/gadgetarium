package gadgetarium.services;

import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Service;

@Service
public interface FirebaseAuthenticationService {
    ResponseEntity<?> authenticateUser(String idToken);
}
