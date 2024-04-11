package gadgetarium.services.impl;

import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseAuthException;
import com.google.firebase.auth.FirebaseToken;
import gadgetarium.entities.User;
import gadgetarium.repositories.UserRepository;
import gadgetarium.services.FirebaseAuthenticationService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Service;

@RequiredArgsConstructor
@Service
public class FirebaseAuthenticationImpl implements FirebaseAuthenticationService {

    private final UserRepository userRepository;

    @Override
    public ResponseEntity<?> authenticateUser(String idToken) {
        try {
            FirebaseToken decodedToken = FirebaseAuth.getInstance().verifyIdToken(idToken);
            String email = decodedToken.getEmail();
            if (email != null) {
                if (!userRepository.existsByEmail(email)) {
                    User newUser = new User();
                    newUser.setEmail(email);
                    userRepository.save(newUser);
                } else {
                    User existingUser = userRepository.findByEmail(email).orElseThrow(()-> new RuntimeException());
                    userRepository.save(existingUser);
                }
            }
            return ResponseEntity.ok().body("User authenticated successfully.");
        } catch (FirebaseAuthException e) {
            return ResponseEntity.status(HttpStatus.UNAUTHORIZED).body("Authentication failed: " + e.getMessage());
        }
    }
}
