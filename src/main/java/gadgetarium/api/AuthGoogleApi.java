package gadgetarium.api;

import gadgetarium.services.FirebaseAuthenticationService;
import io.swagger.v3.oas.annotations.Operation;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequiredArgsConstructor
@RequestMapping("/google")
public class AuthGoogleApi {
    private final FirebaseAuthenticationService firebaseAuthenticationService;

    @Operation(summary = "Вход пользователя через Google", description = "авторизация: Все")
    @PostMapping("/google/authenticate")
    public ResponseEntity<?> authenticateUser(@RequestBody String idToken) {
        return firebaseAuthenticationService.authenticateUser(idToken);
    }
}
